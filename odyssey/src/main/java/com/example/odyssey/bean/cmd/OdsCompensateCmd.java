package com.example.odyssey.bean.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OdsCompensateCmd {

    private String time;
    /**
     * 补偿日期 YYYY-MM-DD
     */
    private String startTime;


    private String endTime;

}
