package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class NftMessageTransferCmd {

    private String to;

    private Long tokenId;
}
