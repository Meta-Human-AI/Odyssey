package com.example.odyssey.common;

import com.example.odyssey.bean.Selector;
import lombok.Getter;


import java.util.ArrayList;
import java.util.List;

@Getter
public enum StateEnum {

    EUROPE(1L, "Europe"),

    AMERICA(2L, "America"),

    ASIA(3L, "Asia"),

    OCEANIA(4L, "Oceania"),

    AFRICA(5L, "Africa");


    private Long code;

    private String name;

    StateEnum(Long code, String name) {
        this.code = code;
        this.name = name;
    }

    public static StateEnum of(Long code) {
        if (code == null) {
            return null;
        }
        for (StateEnum status : StateEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }

    public static StateEnum of(String name) {
        if (name == null) {
            return null;
        }
        for (StateEnum status : StateEnum.values()) {
            if (name.equals(status.name)) {
                return status;
            }
        }
        return null;
    }

    public static List<Selector> list(){

        List<Selector> list = new ArrayList<>();
        for (StateEnum status : StateEnum.values()) {
            Selector selector = new Selector();
            selector.setKey(status.name);
            selector.setValue(status.code.toString());

            list.add(selector);
        }

        return list;
    }

}
