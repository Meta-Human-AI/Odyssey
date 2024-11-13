package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.OperatorDTO;
import com.example.odyssey.bean.dto.OperatorInfoDTO;
import com.example.odyssey.bean.dto.OperatorLoginDTO;
import com.example.odyssey.common.OperatorStatusEnum;
import com.example.odyssey.core.service.OperatorService;
import com.example.odyssey.model.entity.Operator;
import com.example.odyssey.model.mapper.OperatorMapper;
import com.example.odyssey.util.AESUtils;
import com.example.odyssey.util.JwtTokenUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class OperatorServiceImpl implements OperatorService {

    @Resource
    OperatorMapper operatorMapper;

    @Resource
    RedisTemplate redisTemplate;

    @Override
    public SingleResponse createOperator(OperatorCreateCmd operatorCreateCmd) {

        QueryWrapper<Operator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", operatorCreateCmd.getUsername());

        Long count = operatorMapper.selectCount(queryWrapper);
        if (count > 0) {
            return SingleResponse.buildFailure("用户名已存在");
        }
        String password = null;
        try {
            password = AESUtils.aesDecrypt(operatorCreateCmd.getPassword());
        } catch (Exception e) {
            return SingleResponse.buildFailure("密码解密失败");
        }
        Operator operator = new Operator();
        operator.setUsername(operatorCreateCmd.getUsername());
        operator.setPassword(DigestUtils.sha1Hex(password));
        operator.setStatus(OperatorStatusEnum.NORMAL.getCode());
        operatorMapper.insert(operator);

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse chargePassword(OperatorChargePasswordCmd operatorChargePasswordCmd) {

        Operator operator = operatorMapper.selectById(operatorChargePasswordCmd.getId());
        if (operator == null) {
            return SingleResponse.buildFailure("用户不存在");
        }

        String oldPassword = null;
        try {
            oldPassword = AESUtils.aesDecrypt(operatorChargePasswordCmd.getOldPassword());
        } catch (Exception e) {

            return SingleResponse.buildFailure("旧密码解密失败");
        }

        if (!(DigestUtils.sha1Hex(oldPassword).equals(operator.getPassword()))) {
            return SingleResponse.buildFailure("原密码错误");
        }

        String newPassword = null;
        try {
            newPassword = AESUtils.aesDecrypt(operatorChargePasswordCmd.getNewPassword());
        } catch (Exception e) {
            return SingleResponse.buildFailure("新密码解密失败");
        }

        operator.setPassword(DigestUtils.sha1Hex(newPassword));

        operatorMapper.updateById(operator);

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse updateOperator(OperatorUpdateCmd operatorUpdateCmd) {

        Operator operator = operatorMapper.selectById(operatorUpdateCmd.getId());
        if (operator == null) {
            return SingleResponse.buildFailure("用户不存在");
        }

        operator.setStatus(operatorUpdateCmd.getStatus());
        operatorMapper.updateById(operator);

        if (operatorUpdateCmd.getStatus() == OperatorStatusEnum.DISABLE.getCode()) {

            redisTemplate.delete("ODYSSEY:OPERATOR:TOKEN:" + operator.getId());
        }
        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse<OperatorLoginDTO> loginOperator(OperatorLoginCmd operatorLoginCmd) {

        QueryWrapper<Operator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", operatorLoginCmd.getUsername());

        Operator operator = operatorMapper.selectOne(queryWrapper);
        if (operator == null) {
            return SingleResponse.buildFailure("用户不存在");
        }

        String password = null;
        try {
            password = AESUtils.aesDecrypt(operatorLoginCmd.getPassword());
        } catch (Exception e) {
            return SingleResponse.buildFailure("密码解密失败");
        }

        if (!(DigestUtils.sha1Hex(password).equals(operator.getPassword()))) {
            return SingleResponse.buildFailure("密码错误");
        }
        if (operator.getStatus() == OperatorStatusEnum.DISABLE.getCode()) {
            return SingleResponse.buildFailure("用户已禁用");
        }

        String token = JwtTokenUtil.generateToken(operator);

        OperatorLoginDTO operatorLoginDTO = new OperatorLoginDTO();
        operatorLoginDTO.setToken(token);

        //TODO 密码错误次数 限制

        return SingleResponse.of(operatorLoginDTO);
    }

    @Override
    public SingleResponse logoutOperator(OperatorLogoutCmd operatorLogoutCmd) {

        redisTemplate.delete("ODYSSEY:OPERATOR:TOKEN:" + operatorLogoutCmd.getId());

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse<OperatorDTO> queryOperator(OperatorQryCmd operatorQryCmd) {

        Operator operator = operatorMapper.selectById(operatorQryCmd.getId());

        if (Objects.isNull(operator)) {
            return SingleResponse.buildFailure("用户不存在");
        }

        OperatorDTO operatorDTO = new OperatorDTO();
        BeanUtils.copyProperties(operator, operatorDTO);

        return SingleResponse.of(operatorDTO);
    }

    @Override
    public MultiResponse<OperatorDTO> listOperator(OperatorListQryCmd operatorListQryCmd) {

        QueryWrapper<Operator> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(operatorListQryCmd.getUsername())) {
            queryWrapper.like("username", operatorListQryCmd.getUsername());
        }

        Page<Operator> operatorPage = operatorMapper.selectPage(Page.of(operatorListQryCmd.getPageNum(), operatorListQryCmd.getPageSize()), queryWrapper);

        List<OperatorDTO> operatorDTOList = new ArrayList<>();

        for (Operator operator : operatorPage.getRecords()) {
            OperatorDTO operatorDTO = new OperatorDTO();
            BeanUtils.copyProperties(operator, operatorDTO);
            operatorDTOList.add(operatorDTO);
        }

        return MultiResponse.of(operatorDTOList, (int) operatorPage.getTotal());
    }

    @Override
    public SingleResponse<OperatorInfoDTO> queryOperatorInfo(OperatorQryCmd operatorQryCmd) {

        QueryWrapper<Operator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("wallet_address", operatorQryCmd.getWalletAddress());

        Long count = operatorMapper.selectCount(queryWrapper);

        OperatorInfoDTO operatorInfoDTO = new OperatorInfoDTO();
        operatorInfoDTO.setWalletAddress(operatorQryCmd.getWalletAddress());

        if (count > 0) {
            operatorInfoDTO.setHasPermission(true);
        } else {
            operatorInfoDTO.setHasPermission(false);
        }
        return SingleResponse.of(operatorInfoDTO);
    }
}
