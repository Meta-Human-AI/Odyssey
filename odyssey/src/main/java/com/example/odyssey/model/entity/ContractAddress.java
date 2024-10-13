package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ContractAddress {

    @TableId(type = IdType.INPUT)
    private Integer id;
    /**
     * 地址 合约地址
     */
    private String address;
}
