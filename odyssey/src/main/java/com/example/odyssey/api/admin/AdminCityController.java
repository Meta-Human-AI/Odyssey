package com.example.odyssey.api.admin;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.Selector;
import com.example.odyssey.bean.cmd.CityListQryCmd;
import com.example.odyssey.bean.dto.CityDTO;
import com.example.odyssey.common.StateEnum;
import com.example.odyssey.core.service.CityService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/city")
public class AdminCityController {

    @Resource
    CityService cityService;

    @PostMapping("/page")
    MultiResponse<CityDTO> cityPage(@RequestBody CityListQryCmd cityListQryCmd){
        return cityService.cityPage(cityListQryCmd);
    }

    @PostMapping("/list")
    MultiResponse<Selector> cityList(@RequestBody CityListQryCmd cityListQryCmd){
        return cityService.cityList(cityListQryCmd);
    }

    @GetMapping("/state/list")
    MultiResponse<Selector> stateList(){
        return MultiResponse.of(StateEnum.list());
    }
}
