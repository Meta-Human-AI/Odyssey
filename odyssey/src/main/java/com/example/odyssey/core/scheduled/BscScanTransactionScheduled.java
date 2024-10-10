package com.example.odyssey.core.scheduled;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.model.entity.Address;
import com.example.odyssey.model.entity.BscScanTransaction;
import com.example.odyssey.model.entity.SystemConfig;
import com.example.odyssey.model.mapper.AddressMapper;
import com.example.odyssey.model.mapper.BscScanTransactionMapper;
import com.example.odyssey.model.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class BscScanTransactionScheduled {

    @Resource
    private SystemConfigMapper systemConfigMapper;
    @Resource
    private AddressMapper addressMapper;
    @Resource
    private BscScanTransactionMapper bscScanTransactionMapper;

    @Value("${bsc.scan.transaction.url}")
    private String url;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void transactionRecord() {

        QueryWrapper<SystemConfig> systemConfigQueryWrapper = new QueryWrapper<>();
        systemConfigQueryWrapper.eq("key", "apikey");

        SystemConfig systemConfig = systemConfigMapper.selectOne(systemConfigQueryWrapper);
        if (Objects.isNull(systemConfig)) {
            return;
        }

        //根据合约地址 查询购买交易记录
        //todo 还是需要分页查询 否则数据量太大 会影响性能
        Integer PAGE = 1;
        while (true) {

            List<Address> addressList = addressMapper.selectPage(Page.of(PAGE, 500), new QueryWrapper<>()).getRecords();
            if (CollectionUtils.isEmpty(addressList)) {
                return;
            }

            for (Address address : addressList) {

//                QueryWrapper<BscScanTransaction> transactionQueryWrapper = new QueryWrapper<>();
//                transactionQueryWrapper.eq()
//                bscScanTransactionMapper.selectOne()
//
//                Map<String, Object> paramMap = new HashMap<>();
//                paramMap.put("address", address.getAddress());
//                paramMap.put("apikey", systemConfig.getValue());
//                paramMap.put("module", "account");
//                paramMap.put("action", "txlist");
//
//
//                HttpUtil.get()
            }
            PAGE++;
        }

    }

}
