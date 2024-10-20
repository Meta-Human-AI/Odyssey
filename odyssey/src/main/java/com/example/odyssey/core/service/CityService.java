package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.Selector;
import com.example.odyssey.bean.cmd.CityListQryCmd;
import com.example.odyssey.bean.dto.CityDTO;

public interface CityService {


    MultiResponse<CityDTO> cityPage(CityListQryCmd cityListQryCmd);

    MultiResponse<Selector> cityList(CityListQryCmd cityListQryCmd);
}
