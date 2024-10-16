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
@TableName("rebate_config")
public class RebateConfig {

    @TableId(type = IdType.INPUT)
    private Integer id;

    /**
     * 领导地址
     */
    private String address;

    /**
     * 一级推荐人返利比例 10% 直接关联的
     */
    private String firstRebateRate;

    /**
     * 二级推荐人返利比例
     */
    private String secondRebateRate;

    /**
     * 推荐类型
     */
    private String recommendType;

    /**
     * 返利类型
     */
    private String rebateType;


}
