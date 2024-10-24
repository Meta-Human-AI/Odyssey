package com.example.odyssey.core.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.HotelCreateCmd;
import com.example.odyssey.bean.cmd.HotelListQryCmd;
import com.example.odyssey.bean.cmd.HotelUpdateCmd;
import com.example.odyssey.bean.dto.HotelDTO;
import com.example.odyssey.common.StateEnum;
import com.example.odyssey.core.service.HotelService;
import com.example.odyssey.model.entity.City;
import com.example.odyssey.model.entity.Hotel;
import com.example.odyssey.model.entity.NftMessage;
import com.example.odyssey.model.mapper.CityMapper;
import com.example.odyssey.model.mapper.HotelMapper;
import com.example.odyssey.model.mapper.NftMessageMapper;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class HotelServiceImpl implements HotelService {
    @Resource
    HotelMapper hotelMapper;
    @Resource
    NftMessageMapper nftMessageMapper;
    @Resource
    CityMapper cityMapper;

    @Override
    public SingleResponse createHotel(HotelCreateCmd hotelCreateCmd) {

        if (Objects.nonNull(hotelCreateCmd.getStateName())) {
            StateEnum stateEnum = StateEnum.of(hotelCreateCmd.getStateName());
            if (Objects.isNull(stateEnum)) {
                return SingleResponse.buildFailure("所在州不存在");
            }

            hotelCreateCmd.setState(stateEnum.getCode());
        }

        if (Objects.nonNull(hotelCreateCmd.getCityName())) {
            City city = cityMapper.selectOne(new QueryWrapper<City>().eq("name", hotelCreateCmd.getCityName()));
            if (Objects.isNull(city)) {
                return SingleResponse.buildFailure("所在城市不存在");
            }

            hotelCreateCmd.setCity(city.getCode());
        }

        QueryWrapper<Hotel> queryWrapper = new QueryWrapper<>();

        if (Objects.nonNull(hotelCreateCmd.getName())) {
            queryWrapper.eq("name", hotelCreateCmd.getName());
        }
        if (Objects.nonNull(hotelCreateCmd.getState())) {
            queryWrapper.eq("state", hotelCreateCmd.getState());
        }
        if (Objects.nonNull(hotelCreateCmd.getCity())) {
            queryWrapper.eq("city", hotelCreateCmd.getCity());
        }
        if (Objects.nonNull(hotelCreateCmd.getAddress())) {
            queryWrapper.eq("address", hotelCreateCmd.getAddress());
        }

        Hotel hotel = hotelMapper.selectOne(queryWrapper);
        if (Objects.nonNull(hotel)) {
            return SingleResponse.buildSuccess();
        }


        hotel = new Hotel();
        hotel.setName(hotelCreateCmd.getName());
        hotel.setState(hotelCreateCmd.getState());
        hotel.setCity(hotelCreateCmd.getCity());
        hotel.setAddress(hotelCreateCmd.getAddress());
        hotel.setPhone(hotelCreateCmd.getPhone());
        hotel.setEmail(hotelCreateCmd.getEmail());
        hotel.setIntroduction(hotelCreateCmd.getIntroduction());
        hotel.setOfficialWebsite(hotelCreateCmd.getOfficialWebsite());
        hotel.setImage(JSONUtil.toJsonStr(hotelCreateCmd.getImage()));
        hotelMapper.insert(hotel);

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse updateHotel(HotelUpdateCmd hotelUpdateCmd) {

        Hotel hotel = hotelMapper.selectById(hotelUpdateCmd.getId());
        if (Objects.isNull(hotel)) {
            return SingleResponse.buildSuccess();
        }

        hotel.setIntroduction(hotelUpdateCmd.getIntroduction());
        hotel.setCity(hotelUpdateCmd.getCity());
        hotel.setAddress(hotelUpdateCmd.getAddress());
        hotel.setEmail(hotelUpdateCmd.getEmail());
        hotel.setPhone(hotelUpdateCmd.getPhone());
        hotel.setOfficialWebsite(hotelUpdateCmd.getOfficialWebsite());
        hotel.setImage(JSONUtil.toJsonStr(hotelUpdateCmd.getImage()));
        hotelMapper.updateById(hotel);

        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<HotelDTO> pageHotel(HotelListQryCmd hotelListQryCmd) {

        QueryWrapper<Hotel> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(hotelListQryCmd.getName())) {
            queryWrapper.like("name", hotelListQryCmd.getName());
        }
        if (Objects.nonNull(hotelListQryCmd.getState())) {
            queryWrapper.eq("state", hotelListQryCmd.getState());
        }
        if (Objects.nonNull(hotelListQryCmd.getCity())) {
            queryWrapper.eq("city", hotelListQryCmd.getCity());
        }
        if (Objects.nonNull(hotelListQryCmd.getAddress())) {
            queryWrapper.eq("address", hotelListQryCmd.getAddress());
        }
        if (Objects.nonNull(hotelListQryCmd.getPhone())) {
            queryWrapper.eq("phone", hotelListQryCmd.getPhone());
        }
        if (Objects.nonNull(hotelListQryCmd.getEmail())) {
            queryWrapper.eq("email", hotelListQryCmd.getEmail());
        }


        Page<Hotel> hotelPage = hotelMapper.selectPage(Page.of(hotelListQryCmd.getPageNum(), hotelListQryCmd.getPageSize()), queryWrapper);

        List<Long> cityIds = hotelPage.getRecords().stream().map(Hotel::getCity).collect(Collectors.toList());
        if (Collections.isEmpty(cityIds)) {
            return MultiResponse.buildSuccess();
        }

        QueryWrapper<City> cityQueryWrapper = new QueryWrapper<>();
        cityQueryWrapper.in("code", cityIds);
        List<City> cities = cityMapper.selectList(cityQueryWrapper);

        Map<Long, String> cityMap = cities.stream().collect(Collectors.toMap(City::getCode, City::getName));

        List<HotelDTO> hotelList = new ArrayList<>();

        for (Hotel hotel : hotelPage.getRecords()) {
            HotelDTO hotelDTO = new HotelDTO();
            BeanUtils.copyProperties(hotel, hotelDTO);
            hotelDTO.setState(StateEnum.of(hotel.getState()).getName());

            String city = cityMap.get(hotel.getCity());
            hotelDTO.setCity(city);
            hotelDTO.setImage(JSONUtil.toList(hotel.getImage(), String.class));
            hotelList.add(hotelDTO);
        }

        return MultiResponse.of(hotelList, (int) hotelPage.getTotal());
    }

    @Override
    public MultiResponse<HotelDTO> listHotel(HotelListQryCmd hotelListQryCmd) {

        QueryWrapper<NftMessage> nftMessageQueryWrapper = new QueryWrapper<>();
        nftMessageQueryWrapper.eq("token_id", hotelListQryCmd.getTokenId());

        NftMessage nftMessage = nftMessageMapper.selectOne(nftMessageQueryWrapper);
        if (Objects.isNull(nftMessage)) {
            return MultiResponse.buildSuccess();
        }

        QueryWrapper<Hotel> queryWrapper = new QueryWrapper<>();
        if (nftMessage.getState() != 0L) {
            queryWrapper.eq("state", nftMessage.getState());
        }
        if (nftMessage.getCity() != 0L) {
            queryWrapper.eq("city", nftMessage.getCity());
        }

        List<Hotel> hotels = hotelMapper.selectList(queryWrapper);

        if (Collections.isEmpty(hotels)) {
            return MultiResponse.buildSuccess();
        }

        List<Long> cityIds = hotels.stream().map(Hotel::getCity).collect(Collectors.toList());
        if (Collections.isEmpty(cityIds)) {
            return MultiResponse.buildSuccess();
        }
        QueryWrapper<City> cityQueryWrapper = new QueryWrapper<>();
        cityQueryWrapper.in("code", cityIds);
        List<City> cities = cityMapper.selectList(cityQueryWrapper);

        Map<Long, String> cityMap = cities.stream().collect(Collectors.toMap(City::getCode, City::getName));


        List<HotelDTO> hotelList = new ArrayList<>();

        for (Hotel hotel : hotels) {
            HotelDTO hotelDTO = new HotelDTO();
            BeanUtils.copyProperties(hotel, hotelDTO);
            hotelDTO.setState(StateEnum.of(hotel.getState()).getName());
            String city = cityMap.get(hotel.getCity());
            hotelDTO.setCity(city);
            hotelDTO.setImage(JSONUtil.toList(hotel.getImage(), String.class));
            hotelList.add(hotelDTO);
        }
        return MultiResponse.of(hotelList);
    }
}
