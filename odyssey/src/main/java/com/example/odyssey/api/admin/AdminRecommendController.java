package com.example.odyssey.api.admin;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RecommendCreateCmd;
import com.example.odyssey.bean.cmd.RecommendLeaderCreateCmd;
import com.example.odyssey.bean.cmd.RecommendListQryCmd;
import com.example.odyssey.bean.dto.RecommendListDTO;
import com.example.odyssey.core.service.RecommendService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.WalletUtils;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/admin/recommend")
public class AdminRecommendController {

    @Resource
    private RecommendService recommendService;

    @PostMapping("/leader/add")
    public SingleResponse recommendLeader(@RequestBody RecommendLeaderCreateCmd recommendLeaderCreateCmd) {

        Assert.notNull(recommendLeaderCreateCmd.getWalletAddress(), "钱包地址不能为空");

        return recommendService.recommendLeader(recommendLeaderCreateCmd);
    }

    @PostMapping("/list")
    public MultiResponse<RecommendListDTO> getRecommendList(@RequestBody RecommendListQryCmd recommendListQryCmd) {

        return recommendService.getRecommendList(recommendListQryCmd);
    }

    /**
     * 加入
     * @param recommendCreateCmd
     * @return
     */
    @PostMapping("/add")
    SingleResponse recommend(@RequestBody RecommendCreateCmd recommendCreateCmd){

        Assert.notNull(recommendCreateCmd.getWalletAddress(), "钱包地址不能为空");
        Assert.notNull(recommendCreateCmd.getRecommendWalletAddress(), "上级钱包地址不能为空");

        return recommendService.recommend(recommendCreateCmd);
    }
}
