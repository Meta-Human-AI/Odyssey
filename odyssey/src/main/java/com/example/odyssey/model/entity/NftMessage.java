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
@TableName("nft_message")
public class NftMessage {


    @TableId(type = IdType.INPUT)
    private Integer id;

    private Long tokenId;

    private String url;
    /**
     * 等级
     */
    private String type;
    /**
     * 州
     */
    private Long state;
    /**
     * 城市
     */
    private Long city;
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
