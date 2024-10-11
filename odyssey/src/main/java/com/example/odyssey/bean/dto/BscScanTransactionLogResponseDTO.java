package com.example.odyssey.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class BscScanTransactionLogResponseDTO {

    private String status;

    private String message;

    private List<BscScanTransactionLogDTO> result;
}
