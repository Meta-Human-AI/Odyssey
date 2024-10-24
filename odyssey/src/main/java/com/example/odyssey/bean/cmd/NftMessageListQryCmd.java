package com.example.odyssey.bean.cmd;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.PageQuery;
import lombok.Data;

@Data
public class NftMessageListQryCmd extends PageQuery {

    /**
     * 昨天持有人
     */
    private String oldAddress;
    /**
     * 当前持有人
     */
    private String newAddress;
    /**
     * 购买人
     */
    private String buyAddress;

    private Long tokenId;
    /**
     * 等级
     */
    private String type;

    /**
     * 州
     */
    private String state;
    /**
     * 城市
     */
    private String city;


}
