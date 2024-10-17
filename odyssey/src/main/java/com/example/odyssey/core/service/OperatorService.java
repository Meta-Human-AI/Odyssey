package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.OperatorDTO;
import com.example.odyssey.bean.dto.OperatorLoginDTO;

public interface OperatorService {

    SingleResponse createOperator(OperatorCreateCmd operatorCreateCmd);


    SingleResponse chargePassword(OperatorChargePasswordCmd operatorChargePasswordCmd);


    SingleResponse updateOperator(OperatorUpdateCmd operatorUpdateCmd);


    SingleResponse<OperatorLoginDTO> loginOperator(OperatorLoginCmd operatorLoginCmd);


    SingleResponse logoutOperator(OperatorLogoutCmd operatorLogoutCmd);

    SingleResponse<OperatorDTO> queryOperator(OperatorQryCmd operatorQryCmd);

    MultiResponse<OperatorDTO> listOperator(OperatorListQryCmd operatorListQryCmd);


}
