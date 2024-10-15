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

    private String type;

    /**
     * 持有人
     */
    private String address;
}