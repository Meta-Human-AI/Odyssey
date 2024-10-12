package com.example.odyssey.model.mapper;

import com.example.odyssey.config.EasyBaseMapper;
import com.example.odyssey.model.entity.TransactionRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionRecordMapper extends EasyBaseMapper<TransactionRecord> {
}
