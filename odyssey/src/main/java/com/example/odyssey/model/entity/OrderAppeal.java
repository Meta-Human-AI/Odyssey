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
@TableName("order_appeal")
public class OrderAppeal {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 订单id
     */
    private Integer orderId;
    /**
     * 申诉原因
     */
    private String reason;
    /**
     * 申诉状态
     */
    private String status;
    /**
     * 处理说明
     */
    private String remark;
    /**
     * 申诉时间
     */
    private String createTime;
    /**
     * 处理时间
     */
    private String finishTime;

    private String images;
}
