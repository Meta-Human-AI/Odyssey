package com.example.odyssey.common;

import lombok.Getter;

@Getter
public enum AddressTypeEnum {

    CONTRACT("contract", "合约"),

    NFT("nft", "NFT");

    private String code;

    private String name;

    AddressTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AddressTypeEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (AddressTypeEnum status : AddressTypeEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}
