package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class OrderDTO {


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
    private String hotelName;
    /**
     * 酒店所在州
     */
    private String hotelState;

    /**
     * 酒店所在城市
     */
    private String hotelCity;

    /**
     * 酒店所在街道
     */
    private String hotelAddress;
    /**
     * 酒店电话
     */
    private String hotelPhone;
    /**
     * 酒店邮箱
     */
    private String hotelEmail;
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
