package com.example.odyssey.api.user;

import cn.hutool.core.lang.Assert;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.RecommendCoreCreateCmd;
import com.example.odyssey.bean.cmd.RecommendCreateCmd;
import com.example.odyssey.bean.cmd.RecommendListQryCmd;
import com.example.odyssey.bean.dto.RecommendCoreDTO;
import com.example.odyssey.bean.dto.RecommendListDTO;
import com.example.odyssey.core.service.RecommendService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/recommend")
public class RecommendController {

    @Resource
    private RecommendService recommendService;
    @PostMapping("/")
    SingleResponse recommend(@RequestBody RecommendCreateCmd recommendCreateCmd){

        Assert.notNull(recommendCreateCmd.getWalletAddress(), "钱包地址不能为空");
        Assert.notNull(recommendCreateCmd.getRecommendCore(), "推荐码不能为空");

        return recommendService.recommend(recommendCreateCmd);
    }

    @PostMapping("/code")
    SingleResponse<RecommendCoreDTO> getRecommendCore(@RequestBody RecommendCoreCreateCmd recommendCoreCreateCmd){

        Assert.notNull(recommendCoreCreateCmd.getWalletAddress(), "钱包地址不能为空");

        return recommendService.getRecommendCore(recommendCoreCreateCmd);
    }

    @PostMapping("/list")
    public MultiResponse<RecommendListDTO> getRecommendList(@RequestBody RecommendListQryCmd recommendListQryCmd) {

        Assert.notNull(recommendListQryCmd.getWalletAddress(), "钱包地址不能为空");

        return recommendService.getRecommendList(recommendListQryCmd);
    }
}
