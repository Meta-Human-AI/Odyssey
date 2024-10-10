package com.example.odyssey.common;

import lombok.Getter;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.generated.Uint256;

@Getter
public enum FunctionTypeClassEnum {

    Address("address", Address.class),

    Bool("bool", Bool.class),

    Uint256("uint256", Uint256.class);

    private String code;

    private Class type;


    FunctionTypeClassEnum(String code, Class type) {
        this.code = code;
        this.type = type;
    }

    public static FunctionTypeClassEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (FunctionTypeClassEnum status : FunctionTypeClassEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}
