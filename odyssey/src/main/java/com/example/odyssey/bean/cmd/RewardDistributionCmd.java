package com.example.odyssey.bean.cmd;

import com.example.odyssey.bean.dto.RewardDistributionDTO;
import lombok.Data;

import java.util.List;

/**
 * ods奖励分配
 */
@Data
public class RewardDistributionCmd {

    List<RewardDistributionDTO> list;
}
