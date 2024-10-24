package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class NftMessageDTO {

    private Integer id;

    private Long tokenId;

    private String url;

    private String type;

    /**
     * 州
     */
    private String state;
    /**
     * 城市
     */
    private String city;

    /**
     * 封锁时间
     */
    private Long blockadeTime;

    /**
     * 昨天持有人
     */
    private String oldAddress;
    /**
     * 当前持有人
     */
    private String newAddress;
    /**
     * 购买人
     */
    private String buyAddress;

    /**
     * 购买时间
     */
    private String buyTime;
}
