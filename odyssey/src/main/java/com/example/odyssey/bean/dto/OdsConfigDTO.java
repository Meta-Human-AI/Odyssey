package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class OdsConfigDTO {

    /**
     * ods 等级类型 OL OS OA OB
     */
    private String type;

    /**
     * ods 每个等级每天 发放比例
     */
    private Integer rate;

    /**
     * ods 每个等级每天 发放数量
     */
    private Integer number;
}
