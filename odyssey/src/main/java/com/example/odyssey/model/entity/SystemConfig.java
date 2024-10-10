package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class SystemConfig {

    @TableId(type = IdType.INPUT)
    private Integer id;

    private String key;

    private String value;
}
