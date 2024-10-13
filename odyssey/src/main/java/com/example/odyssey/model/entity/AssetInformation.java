package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class AssetInformation {

    @TableId(type = IdType.INPUT)
    private Integer id;

    private Long tokenId;

    private String type;
}
