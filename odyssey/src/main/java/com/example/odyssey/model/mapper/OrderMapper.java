package com.example.odyssey.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.odyssey.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
