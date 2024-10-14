package com.example.odyssey.util;

import com.example.odyssey.common.NftLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class Web3jUtil {

    @Resource
    private Web3j web3j;

    public String getNftIdToLevel(Long tokenId, String address) {

        try {

            List input = Arrays.asList(new Uint256(tokenId));

            List output = Arrays.asList(new TypeReference<Uint256>() {
            });

            Function function = new Function("nftIdToLevel", input, output);

            String data = FunctionEncoder.encode(function);

            Transaction transaction = Transaction.createEthCallTransaction(null, address, data);

            EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();

            if (Objects.isNull(response.getValue())) {
                log.error("getNftIdToLevel tokenId :{} ,response is error:{}", tokenId, response.getError().getMessage());
                return null;
            }

            Long type = Long.valueOf(response.getValue().substring(2), 16);

            return NftLevelEnum.of(type).getName();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }
}
