package com.example.odyssey.common;

import lombok.Getter;

@Getter
public enum NftLevelEnum {

    OL(1L, "OL"),

    OS(2L, "OS"),

    OA(3L, "OA"),

    OB(4L, "OB");

    private Long code;

    private String name;

    NftLevelEnum(Long code, String name) {
        this.code = code;
        this.name = name;
    }

    public static NftLevelEnum of(Long code) {
        if (code == null) {
            return null;
        }
        for (NftLevelEnum status : NftLevelEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}
