package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.HotelCreateCmd;
import com.example.odyssey.bean.cmd.HotelListQryCmd;
import com.example.odyssey.bean.dto.HotelDTO;

public interface HotelService {

    SingleResponse createHotel(HotelCreateCmd hotelCreateCmd);


    MultiResponse<HotelDTO> pageHotel(HotelListQryCmd hotelListQryCmd);


    MultiResponse<HotelDTO> listHotel(HotelListQryCmd hotelListQryCmd);
}
