package com.example.odyssey.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class OdsConfig {

    @TableId(type = IdType.AUTO)
    private Integer id;
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
