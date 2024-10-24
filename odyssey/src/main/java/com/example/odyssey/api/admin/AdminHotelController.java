package com.example.odyssey.api.admin;

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
@RequestMapping("/v1/admin/hotel")
public class AdminHotelController {


    @Resource
    HotelService hotelService;
    @Resource
    MinioUtils minioUtils;

    @SneakyThrows
    @PostMapping("/import")
    public SingleResponse importHotel(@RequestBody MultipartFile file) {

        EasyExcel.read(file.getInputStream(), HotelImportDTO.class, new PageReadListener<HotelImportDTO>(dataList -> {

            for (HotelImportDTO hotelImportDTO : dataList) {
                HotelCreateCmd hotelCreateCmd = new HotelCreateCmd();
                BeanUtils.copyProperties(hotelImportDTO, hotelCreateCmd);
                hotelService.createHotel(hotelCreateCmd);
            }

        })).sheet().doRead();
        return SingleResponse.buildSuccess();
    }

    @PostMapping("/page")
    public MultiResponse<HotelDTO> pageHotel(@RequestBody HotelListQryCmd hotelListQryCmd) {
        return hotelService.pageHotel(hotelListQryCmd);
    }

    @PostMapping("/list")
    public MultiResponse<HotelDTO> listHotel(@RequestBody HotelListQryCmd hotelListQryCmd) {

        Assert.notNull(hotelListQryCmd.getTokenId(), "tokenId不能为空");
        return hotelService.listHotel(hotelListQryCmd);
    }

    @PostMapping("/add")
    public SingleResponse createHotel(@RequestBody HotelCreateCmd hotelCreateCmd) {
        Assert.notNull(hotelCreateCmd.getName(), "酒店名称不能为空");
        Assert.notNull(hotelCreateCmd.getState(), "所在州不能为空");
        Assert.notNull(hotelCreateCmd.getCity(), "所在城市不能为空");
        Assert.notNull(hotelCreateCmd.getAddress(), "详细地址不能为空");
        Assert.notNull(hotelCreateCmd.getPhone(), "酒店手机号不能为空");
        Assert.notNull(hotelCreateCmd.getEmail(), "酒店邮箱不能为空");

        return hotelService.createHotel(hotelCreateCmd);
    }
    @PostMapping("/update")
    public SingleResponse updateHotel(@RequestBody HotelUpdateCmd hotelUpdateCmd){
        Assert.notNull(hotelUpdateCmd.getId(), "酒店id不能为空");
        Assert.notNull(hotelUpdateCmd.getName(), "酒店名称不能为空");
        Assert.notNull(hotelUpdateCmd.getState(), "所在州不能为空");
        Assert.notNull(hotelUpdateCmd.getCity(), "所在城市不能为空");
        Assert.notNull(hotelUpdateCmd.getAddress(), "详细地址不能为空");
        Assert.notNull(hotelUpdateCmd.getPhone(), "酒店手机号不能为空");
        Assert.notNull(hotelUpdateCmd.getEmail(), "酒店邮箱不能为空");

        return hotelService.updateHotel(hotelUpdateCmd);
    }


    @PostMapping("/upload")
    public SingleResponse<String> upload(@RequestBody MultipartFile file) {
        try {
            String hotel = minioUtils.upload("hotel", file);
            return SingleResponse.of(hotel);
        }catch (Exception e) {
            e.printStackTrace();
            return SingleResponse.buildFailure("文件上传异常");
        }
    }
}
