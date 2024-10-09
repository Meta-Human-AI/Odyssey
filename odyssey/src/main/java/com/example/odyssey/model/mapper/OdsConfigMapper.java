package com.example.odyssey.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.odyssey.model.entity.OdsConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * ods 挖矿每天每个等级发放ods配置
 */
@Mapper
public interface OdsConfigMapper extends BaseMapper<OdsConfig> {
}
