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
@TableName("`nft_daily_statistics`")
public class NftDailyStatistics {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 统计日期
     */
    private String date;

    /**
     * OL总数
     */
    private Integer olTotal;

    /**
     * OS总数
     */
    private Integer osTotal;

    /**
     * OA总数
     */
    private Integer oaTotal;

    /**
     * OB总数
     */
    private Integer obTotal;

    /**
     * OL新增
     */
    private Integer olNew;

    /**
     * OS新增
     */
    private Integer osNew;

    /**
     * OA新增
     */
    private Integer oaNew;

    /**
     * OB新增
     */
    private Integer obNew;

    /**
     * 更新时间
     */
    private String updateTime;
}
