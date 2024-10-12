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
@TableName("transaction_record")
public class TransactionRecord {

    @TableId(type = IdType.INPUT)
    private Integer id;

    private String walletAddress;

    private String time;

    private String action;

    private String type;

    private Integer number;
}
