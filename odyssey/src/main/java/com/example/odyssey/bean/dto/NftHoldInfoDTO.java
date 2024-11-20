package com.example.odyssey.bean.dto;

import com.example.odyssey.model.entity.Recommend;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NftHoldInfoDTO {

    private Long tokenId;
    private String type;
    private String walletAddress;
    private String transferTime;
    private Recommend recommend;  // 持有人的推荐关系
}
