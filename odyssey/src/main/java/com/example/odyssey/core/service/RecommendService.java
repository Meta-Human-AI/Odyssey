package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.RecommendCoreDTO;
import com.example.odyssey.bean.dto.RecommendDTO;
import com.example.odyssey.bean.dto.RecommendListDTO;

public interface RecommendService {
    /**
     * 获取推荐码
     * @param recommendCoreCreateCmd
     * @return
     */
    SingleResponse<RecommendCoreDTO> getRecommendCore(RecommendCoreCreateCmd recommendCoreCreateCmd);

    /**
     * 推荐
     * @param recommendCreateCmd
     * @return
     */
    SingleResponse recommend(RecommendCreateCmd recommendCreateCmd);

    /**
     * 推荐领导
     * @param recommendLeaderCreateCmd
     * @return
     */
    SingleResponse recommendLeader(RecommendLeaderCreateCmd recommendLeaderCreateCmd);

    /**
     * 获取推荐列表 树状结构
     * @param recommendListQryCmd
     * @return
     */
    MultiResponse<RecommendListDTO> getRecommendList(RecommendListQryCmd recommendListQryCmd);


    SingleResponse<RecommendDTO> getRecommend(RecommendQryCmd recommendQryCmd);

}
