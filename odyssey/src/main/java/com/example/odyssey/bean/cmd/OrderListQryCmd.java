package com.example.odyssey.bean.cmd;

import com.example.odyssey.bean.PageQuery;
import lombok.Data;

@Data
public class OrderListQryCmd extends PageQuery {

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
     * 酒店所在州
     */
    private Long hotelState;

    /**
     * 酒店所在城市
     */
    private Long hotelCity;

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
    private String createStartTime;

    private String createEndTime;
    /**
     * 取消时间
     */
    private String cancelStartTime;

    private String cancelEndTime;
    /**
     * 完成时间
     */
    private String finishStartTime;

    private String finishEndTime;
    /**
     * 审核时间
     */
    private String examineStartTime;

    private String examineEndTime;

    private Boolean isAdmin;
}
