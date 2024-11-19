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
@TableName("recommend_core_log")
public class RecommendCoreLog {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 推荐人钱包地址
     */
    private String walletAddress;
    /**
     * 推荐码
     */
    private String recommendCore;
    /**
     * 推荐码生成时间
     */
    private Long createTime;
    /**
     * 推荐码过期时间
     */
    private Long expireTime;
}
