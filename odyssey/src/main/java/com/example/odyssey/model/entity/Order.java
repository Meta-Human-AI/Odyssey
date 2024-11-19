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
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 钱包地址
     */
    private String address;
    /**
     * nft token id
     */
    private Long tokenId;
    /**
     * 酒店
     */
    private Integer hotelId;
    /**
     * 入住时间
     */
    private String startTime;
    /**
     * 离店时间
     */
    private String endTime;
    /**
     * 入住数量
     */
    private Integer number;
    /**
     * 联系人
     */
    private String name;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 订单状态
     */
    private String status;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 取消时间
     */
    private String cancelTime;
    /**
     * 完成时间
     */
    private String finishTime;
    /**
     * 审核时间
     */
    private String examineTime;
    /**
     * 取消或审批拒绝原因
     */
    private String reason;
}
