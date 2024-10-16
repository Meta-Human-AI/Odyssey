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

    private Long tokenId;
}
