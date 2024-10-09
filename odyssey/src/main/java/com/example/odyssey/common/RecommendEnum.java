package com.example.odyssey.common;


import lombok.Getter;

/**
 * 推荐类型
 * 推荐人体系往上三级的推荐人中，包含领导者时，属于领导推荐，否则属于员工推荐
 */
@Getter
public enum RecommendEnum {

    NORMAL("normal", "员工推荐"),

    LEADER("leader", "领导推荐");

    private String code;

    private String name;

    RecommendEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RecommendEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (RecommendEnum status : RecommendEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}
