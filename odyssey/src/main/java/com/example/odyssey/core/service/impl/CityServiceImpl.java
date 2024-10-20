package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.Selector;
import com.example.odyssey.bean.cmd.CityListQryCmd;
import com.example.odyssey.bean.dto.CityDTO;
import com.example.odyssey.common.StateEnum;
import com.example.odyssey.core.service.CityService;
import com.example.odyssey.model.entity.City;
import com.example.odyssey.model.mapper.CityMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class CityServiceImpl implements CityService {

    @Resource
    CityMapper cityMapper;

    @Override
    public MultiResponse<CityDTO> cityPage(CityListQryCmd cityListQryCmd) {

        QueryWrapper<City> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(cityListQryCmd.getName())){
            queryWrapper.like("name",cityListQryCmd.getName());
        }
        if (Objects.nonNull(cityListQryCmd.getState())){
            queryWrapper.eq("state",cityListQryCmd.getState());
        }

        Page<City> cityPage = cityMapper.selectPage(Page.of(cityListQryCmd.getPageNum(), cityListQryCmd.getPageSize()), queryWrapper);

        List<CityDTO> cityDTOList = new ArrayList<>();
        for (City city : cityPage.getRecords()){

            CityDTO cityDTO = new CityDTO();
            BeanUtils.copyProperties(city,cityDTO);
            cityDTO.setStateName(StateEnum.of(city.getState()).getName());

            cityDTOList.add(cityDTO);
        }


        return MultiResponse.of(cityDTOList, (int) cityPage.getTotal());
    }

    @Override
    public MultiResponse<Selector> cityList(CityListQryCmd cityListQryCmd) {

        QueryWrapper<City> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(cityListQryCmd.getName())){
            queryWrapper.like("name",cityListQryCmd.getName());
        }
        if (Objects.nonNull(cityListQryCmd.getState())){
            queryWrapper.eq("state",cityListQryCmd.getState());
        }

        List<Selector> list = new ArrayList<>();

        List<City> cities = cityMapper.selectList(queryWrapper);
        for (City city : cities){

            Selector selector = new Selector();
            selector.setKey(city.getName());
            selector.setValue(city.getCode().toString());

            list.add(selector);
        }

        return MultiResponse.of(list);
    }
}
