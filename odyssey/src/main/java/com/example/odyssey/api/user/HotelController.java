package com.example.odyssey.api.user;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.HotelCreateCmd;
import com.example.odyssey.bean.cmd.HotelListQryCmd;
import com.example.odyssey.bean.cmd.HotelUpdateCmd;
import com.example.odyssey.bean.dto.HotelDTO;
import com.example.odyssey.bean.dto.HotelImportDTO;
import com.example.odyssey.core.service.HotelService;
import com.example.odyssey.util.MinioUtils;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/hotel")
public class HotelController {


    @Resource
    HotelService hotelService;


    @PostMapping("/page")
    public MultiResponse<HotelDTO> pageHotel(@RequestBody HotelListQryCmd hotelListQryCmd) {
        return hotelService.pageHotel(hotelListQryCmd);
    }

    @PostMapping("/list")
    public MultiResponse<HotelDTO> listHotel(@RequestBody HotelListQryCmd hotelListQryCmd) {

        Assert.notNull(hotelListQryCmd.getTokenId(), "tokenId不能为空");
        return hotelService.listHotel(hotelListQryCmd);
    }
}
