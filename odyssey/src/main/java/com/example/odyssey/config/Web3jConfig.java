package com.example.odyssey.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class Web3jConfig {

    @Value("${web3j.ethereum.node.url}")
    private String ethereumNodeUrl;


    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(ethereumNodeUrl));
    }


    public static void main(String[] args) throws IOException {

        String address = "0x263ec0Cee70Fa01c154A63695877371185FEe886";

        Web3j web3j = Web3j.build(new HttpService("https://bsc-testnet.infura.io/v3/4c223b9e87754809a5d8f819a261fdb7"));

        List input = Arrays.asList(new Uint256(3));

        List output = Arrays.asList(new TypeReference<Uint256>() {
        });

        Function function = new Function("nftIdToLevel", input, output);

        String data = FunctionEncoder.encode(function);

        Transaction transaction = Transaction.createEthCallTransaction(null, address, data);

        EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();

        boolean reverted = response.isReverted();

        System.out.println(reverted);

        List<Type> list = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());

        for (Type type : list) {
            System.out.println(type.getValue());
        }
    }
}
