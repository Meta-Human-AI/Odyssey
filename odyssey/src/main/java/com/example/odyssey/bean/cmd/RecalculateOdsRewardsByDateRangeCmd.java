package com.example.odyssey.bean.cmd;

import lombok.Data;

@Data
public class RecalculateOdsRewardsByDateRangeCmd {

    /**
     * 开始日期 2024-11-17
     */
    private String startDate;
    /**
     * 结束日期 2024-11-17
     */
    private String endDate;
}
