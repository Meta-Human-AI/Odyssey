package com.example.odyssey.bean.dto;

import com.example.odyssey.model.entity.BscScanAccountTransaction;
import lombok.Data;

import java.util.List;

/**
 * BacAcan account transaction response
 */
@Data
public class BscScanAccountTransactionResponseDTO {

    private String status;

    private String message;

    private List<BscScanAccountTransactionDTO> result;
}
