package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.common.OrderAppealStatusEnum;
import com.example.odyssey.common.OrderStatusEnum;
import com.example.odyssey.core.service.EmailService;
import com.example.odyssey.core.service.OrderService;
import com.example.odyssey.model.entity.NftMessage;
import com.example.odyssey.model.entity.Order;
import com.example.odyssey.model.entity.OrderAppeal;
import com.example.odyssey.model.entity.SystemConfig;
import com.example.odyssey.model.mapper.NftMessageMapper;
import com.example.odyssey.model.mapper.OrderAppealMapper;
import com.example.odyssey.model.mapper.OrderMapper;
import com.example.odyssey.model.mapper.SystemConfigMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Resource
    OrderMapper orderMapper;
    @Resource
    RedissonClient redissonClient;
    @Resource
    OrderAppealMapper orderAppealMapper;
    @Resource
    EmailService emailService;
    @Resource
    NftMessageMapper nftMessageMapper;
    @Resource
    SystemConfigMapper systemConfigMapper;
    @Override
    public SingleResponse createOrder(OrderCreateCmd orderCreateCmd) {

        RLock redLock = redissonClient.getLock("odyssey:create:order:" + orderCreateCmd.getTokenId());

        try {
            if (redLock.tryLock(5 * 1000, TimeUnit.MILLISECONDS)) {

                QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("token_id", orderCreateCmd.getTokenId());
                NftMessage nftMessage = nftMessageMapper.selectOne(queryWrapper);
                if (nftMessage == null) {
                    return SingleResponse.buildFailure("nft不存在");
                }

                if (nftMessage.getBlockadeTime() > System.currentTimeMillis()) {
                    return SingleResponse.buildFailure("nft已被封锁");
                }

                //todo 是否需要做订单重复判断

                Order order = new Order();
                BeanUtils.copyProperties(orderCreateCmd, order);
                order.setStatus(OrderStatusEnum.AUTHENTICATION.getCode());
                order.setCreateTime(LocalDateTime.now().toString());
                orderMapper.insert(order);

                //todo 发送邮件
                EmailCreateCmd emailCreateCmd = new EmailCreateCmd();
                emailCreateCmd.setEmail(orderCreateCmd.getEmail());
                emailCreateCmd.setOrderId(order.getId());

                emailService.sendEmail(emailCreateCmd);

                Integer blockadeDay = 90;

                QueryWrapper<SystemConfig> systemQueryWrapper = new QueryWrapper<>();

                systemQueryWrapper.eq("key", "nft_blockade_day");

                SystemConfig systemConfig = systemConfigMapper.selectOne(systemQueryWrapper);

                if (Objects.nonNull(systemConfig)) {
                    blockadeDay = Integer.valueOf(systemConfig.getValue());
                }

                nftMessage.setBlockadeTime(System.currentTimeMillis() + blockadeDay * 24 * 60 * 60 * 1000);

                nftMessageMapper.updateById(nftMessage);

                return SingleResponse.buildSuccess();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (redLock.isHeldByCurrentThread()) {
                redLock.unlock();
            }
        }
        return SingleResponse.buildFailure("创建订单失败");
    }

    @Override
    public SingleResponse cancelOrder(OrderCancelCmd orderCancelCmd) {

        RLock redLock = redissonClient.getLock("odyssey:order:" + orderCancelCmd.getOrderId());

        try {
            if (redLock.tryLock(5 * 1000, TimeUnit.MILLISECONDS)) {

                Order order = orderMapper.selectById(orderCancelCmd.getOrderId());
                if (order == null) {
                    return SingleResponse.buildFailure("订单不存在");
                }

                if (order.getStatus().equals(OrderStatusEnum.AUTHENTICATION.getCode()) || order.getStatus().equals(OrderStatusEnum.EXAMINE.getCode())) {

                    order.setReason(orderCancelCmd.getReason());
                    order.setCancelTime(LocalDateTime.now().toString());
                    order.setStatus(OrderStatusEnum.CANCEL.getCode());
                    orderMapper.updateById(order);

                    QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("token_id", order.getTokenId());
                    NftMessage nftMessage = nftMessageMapper.selectOne(queryWrapper);

                    if (Objects.nonNull(nftMessage)) {
                        nftMessage.setBlockadeTime(0L);
                        nftMessageMapper.updateById(nftMessage);
                    }

                    return SingleResponse.buildSuccess();
                }

                return SingleResponse.buildFailure("订单状态不正确");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (redLock.isHeldByCurrentThread()) {
                redLock.unlock();
            }
        }
        return SingleResponse.buildFailure("取消订单失败");
    }

    @Override
    public SingleResponse examineOrder(OrderExamineCmd orderExamineCmd) {

        RLock redLock = redissonClient.getLock("odyssey:order:" + orderExamineCmd.getOrderId());

        try {
            if (redLock.tryLock(5 * 1000, TimeUnit.MILLISECONDS)) {

                Order order = orderMapper.selectById(orderExamineCmd.getOrderId());
                if (order == null) {
                    return SingleResponse.buildFailure("订单不存在");
                }

                if (order.getStatus() != OrderStatusEnum.EXAMINE.getCode()) {
                    return SingleResponse.buildFailure("订单状态不正确");
                }

                order.setExamineTime(LocalDateTime.now().toString());
                order.setStatus(orderExamineCmd.getStatus());
                order.setReason(orderExamineCmd.getReason());

                orderMapper.updateById(order);

                return SingleResponse.buildSuccess();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (redLock.isHeldByCurrentThread()) {
                redLock.unlock();
            }
        }
        return SingleResponse.buildFailure("审核订单失败");
    }

    @Override
    public SingleResponse appealOrder(OrderAppealCreateCmd orderAppealCreateCmd) {

        OrderAppeal orderAppeal = new OrderAppeal();
        BeanUtils.copyProperties(orderAppealCreateCmd, orderAppeal);
        orderAppeal.setStatus(OrderAppealStatusEnum.PENDING.getCode());
        orderAppeal.setCreateTime(LocalDateTime.now().toString());
        orderAppealMapper.insert(orderAppeal);

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse cancelAppealOrder(OrderAppealCancelCmd orderAppealCancelCmd) {

        OrderAppeal orderAppeal = orderAppealMapper.selectById(orderAppealCancelCmd.getOrderAppealId());
        if (orderAppeal == null) {
            return SingleResponse.buildFailure("申诉订单不存在");
        }

        if (orderAppeal.getStatus() != OrderAppealStatusEnum.PENDING.getCode()) {
            return SingleResponse.buildFailure("申诉订单状态不正确");
        }

        orderAppeal.setStatus(OrderAppealStatusEnum.CANCEL.getCode());
        orderAppeal.setRemark(orderAppealCancelCmd.getRemark());
        orderAppealMapper.updateById(orderAppeal);

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse handleAppealOrder(OrderAppealHandleCmd orderAppealHandleCmd) {

        OrderAppeal orderAppeal = orderAppealMapper.selectById(orderAppealHandleCmd.getOrderAppealId());

        if (orderAppeal == null) {
            return SingleResponse.buildFailure("申诉订单不存在");
        }

        if (orderAppeal.getStatus() != OrderAppealStatusEnum.PENDING.getCode()) {
            return SingleResponse.buildFailure("申诉订单状态不正确");
        }

        orderAppeal.setRemark(orderAppealHandleCmd.getRemark());
        orderAppeal.setStatus(OrderAppealStatusEnum.FINISH.getCode());
        orderAppeal.setFinishTime(LocalDateTime.now().toString());

        orderAppealMapper.updateById(orderAppeal);
        return SingleResponse.buildSuccess();
    }
}
