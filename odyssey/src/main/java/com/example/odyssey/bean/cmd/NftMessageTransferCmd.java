package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class NftMessageTransferCmd {

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

    private String bugTime;

    /**
     * 转入时间
     */
    private String transferTime;
    /**
     * 空投时间
     */
    private String airdropTime;

    private Long tokenId;
}
