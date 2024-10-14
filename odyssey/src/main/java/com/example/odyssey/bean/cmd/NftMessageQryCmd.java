package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class NftMessageQryCmd {
    /**
     * 合约地址
     */
    private String address;
    /**
     * nft token id
     */
    private Long tokenId;
}
