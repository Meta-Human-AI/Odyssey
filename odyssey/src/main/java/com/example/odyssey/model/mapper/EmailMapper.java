package com.example.odyssey.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.odyssey.model.entity.Email;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailMapper extends BaseMapper<Email> {
}
