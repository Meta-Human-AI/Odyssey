package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.HotelCreateCmd;
import com.example.odyssey.bean.cmd.HotelListQryCmd;
import com.example.odyssey.bean.dto.HotelDTO;
import com.example.odyssey.common.StateEnum;
import com.example.odyssey.core.service.HotelService;
import com.example.odyssey.model.entity.City;
import com.example.odyssey.model.entity.Hotel;
import com.example.odyssey.model.entity.NftMessage;
import com.example.odyssey.model.mapper.CityMapper;
import com.example.odyssey.model.mapper.HotelMapper;
import com.example.odyssey.model.mapper.NftMessageMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class HotelServiceImpl implements HotelService {
    @Resource
    HotelMapper hotelMapper;
    @Resource
    NftMessageMapper nftMessageMapper;

    @Override
    public SingleResponse createHotel(HotelCreateCmd hotelCreateCmd) {

        QueryWrapper<Hotel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", hotelCreateCmd.getName());
        queryWrapper.eq("state",hotelCreateCmd.getState());
        queryWrapper.eq("city",hotelCreateCmd.getCity());
        queryWrapper.eq("address",hotelCreateCmd.getAddress());

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

        hotelMapper.insert(hotel);

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
        if (Objects.nonNull(hotelListQryCmd.getType())) {
            queryWrapper.eq("type", hotelListQryCmd.getType());
        }

        Page<Hotel> hotelPage = hotelMapper.selectPage(Page.of(hotelListQryCmd.getPageNum(), hotelListQryCmd.getPageSize()), queryWrapper);

        List<HotelDTO> hotelList = new ArrayList<>();

        for (Hotel hotel : hotelPage.getRecords()) {
            HotelDTO hotelDTO = new HotelDTO();
            BeanUtils.copyProperties(hotel, hotelDTO);
            hotelList.add(hotelDTO);
        }

        return MultiResponse.of(hotelList, (int)hotelPage.getTotal());
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
        if (nftMessage.getState() != 0L){
            queryWrapper.eq("state", nftMessage.getState());
        }
        if (nftMessage.getCity() != 0L){
            queryWrapper.eq("city",nftMessage.getCity());
        }

        List<Hotel> hotels = hotelMapper.selectList(queryWrapper);

        List<HotelDTO> hotelList = new ArrayList<>();

        for (Hotel hotel : hotels) {
            HotelDTO hotelDTO = new HotelDTO();
            BeanUtils.copyProperties(hotel, hotelDTO);
            hotelList.add(hotelDTO);
        }
        return MultiResponse.of(hotelList);
    }
}
