package com.example.odyssey.bean.cmd;

import com.example.odyssey.bean.PageQuery;
import lombok.Data;

@Data
public class RewardDistributionListQryCmd extends PageQuery {


    /**
     * 钱包地址
     */
    private String walletAddress;
    /**
     * 关联地址 因为谁 而获得返利
     */
    private String relationAddress;
    /**
     * nft
     */
    private Long tokenId;

    /**
     * 奖励类型
     */
    private String rewardType;

    /**
     * 奖励状态
     */
    private String rewardStatus;

}
