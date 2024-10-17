package com.example.odyssey.bean.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class SystemConfigDTO {

    private Integer id;

    private String key;

    private String value;
}
