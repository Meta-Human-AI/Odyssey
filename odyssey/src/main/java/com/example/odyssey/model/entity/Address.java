package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Address {

    @TableId(type = IdType.INPUT)
    private Integer id;
    /**
     * 地址 合约地址 或者 nft地址
     */
    private String address;

    /**
     * 地址类型
     */
    private String type;
}
