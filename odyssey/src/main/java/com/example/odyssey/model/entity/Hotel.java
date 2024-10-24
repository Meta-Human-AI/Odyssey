package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("`hotel`")
public class Hotel {

    @TableId(type = IdType.INPUT)
    private Integer id;
    /**
     * 名称
     */
    private String name;
    /**
     * 州
     */
    private Long state;
    /**
     * 城市
     */
    private Long city;
    /**
     * 地址
     */
    private String address;
    /**
     * 电话
     */
    private String phone;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 简介
     */
    private String introduction;
    /**
     * 图片
     */
    private String image;
    /**
     * 官网
     */
    private String officialWebsite;
}
