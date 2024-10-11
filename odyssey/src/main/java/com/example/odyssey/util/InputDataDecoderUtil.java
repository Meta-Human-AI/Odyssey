package com.example.odyssey.util;

import cn.hutool.json.JSONUtil;
import com.example.odyssey.bean.dto.BscScanTransactionLogDTO;
import com.example.odyssey.bean.dto.TransferFromDTO;
import com.example.odyssey.bean.dto.TransferLogDTO;
import com.example.odyssey.common.FunctionTypeClassEnum;
import com.example.odyssey.model.entity.BscScanAccountTransaction;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

import java.util.*;


public class InputDataDecoderUtil {

    public static String BscScanLogTransaction(BscScanTransactionLogDTO bscScanTransactionLogDTO) {

        List<String> topics = bscScanTransactionLogDTO.getTopics();
        if (CollectionUtils.isEmpty(topics) || topics.size() != 4) {
            return "";
        }
        List<TransferLogDTO> transferLogList = new ArrayList<>();

        int fromLength = topics.get(1).length();
        String from = "0x" + topics.get(1).substring(fromLength - 40, fromLength);

        System.out.println(from);

        TransferLogDTO fromTransferLogDTO = new TransferLogDTO();
        fromTransferLogDTO.setName("from");
        fromTransferLogDTO.setData(from);

        int toLength = topics.get(2).length();
        String to = "0x" + topics.get(2).substring(toLength - 40, toLength);

        System.out.println(to);

        TransferLogDTO toTransferLogDTO = new TransferLogDTO();
        toTransferLogDTO.setName("to");
        toTransferLogDTO.setData(to);


        Integer tokenId = Integer.parseInt(topics.get(3).substring(2), 16);

        TransferLogDTO tokenIdTransferLogDTO = new TransferLogDTO();
        tokenIdTransferLogDTO.setName("tokenId");
        tokenIdTransferLogDTO.setData(tokenId.toString());

        transferLogList.add(fromTransferLogDTO);
        transferLogList.add(toTransferLogDTO);
        transferLogList.add(tokenIdTransferLogDTO);

        return JSONUtil.toJsonStr(transferLogList);

    }

    public static void BscScanAccountTransaction(BscScanAccountTransaction bscScanAccountTransaction) {

        if (!StringUtils.hasLength(bscScanAccountTransaction.getFunctionName())) {
            return;
        }

        try {
            // 去除方法签名
            String input = bscScanAccountTransaction.getInput().substring(bscScanAccountTransaction.getMethodId().length());

            // 解析方法 获取 参数类型
            String functionName = bscScanAccountTransaction.getFunctionName();

            String[] attributeList = functionName.substring(functionName.indexOf("(") + 1, functionName.indexOf(")")).split(", ");

            List<TypeReference<?>> outputParameters = new ArrayList<>();

            List<TransferFromDTO> transferFromList = new ArrayList<>();

            for (String attribute : attributeList) {

                String[] parameter = attribute.split(" ");

                FunctionTypeClassEnum functionTypeClassEnum = FunctionTypeClassEnum.of(parameter[0]);

                if (functionTypeClassEnum == null) {
                    return;
                }

                outputParameters.add(TypeReference.create(functionTypeClassEnum.getType()));

                TransferFromDTO transferFromDTO = new TransferFromDTO();
                transferFromDTO.setName(parameter[1]);
                transferFromDTO.setType(parameter[0]);

                transferFromList.add(transferFromDTO);

            }

            Function function = new Function(bscScanAccountTransaction.getFunctionName(), new ArrayList<>(), outputParameters);

            List<Type> list = FunctionReturnDecoder.decode(input, function.getOutputParameters());

            for (int i = 0; i < list.size(); i++) {

                TransferFromDTO transferFromDTO = transferFromList.get(i);
                transferFromDTO.setData(list.get(i).getValue().toString());
            }

            bscScanAccountTransaction.setDecodedInput(JSONUtil.toJsonStr(transferFromList));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {

//        String  input = "00000000000000000000000052ed23e74e802ade6a7d64fe773cb30c1d1177230000000000000000000000000000000000000000000000000000000000000001";
//
//        Function function = new Function("setApprovalForAll", new ArrayList<>(), Arrays.asList(new TypeReference<Address>(){}, new TypeReference<Bool>(){}) );
//
//        List<Type> list = FunctionReturnDecoder.decode(input, function.getOutputParameters());
//
//        for (Type type : list) {
//            System.out.println(type.getValue().toString());
//        }


//        String input = "0000000000000000000000008c5547aa85b61e7060052e7b5bbe8862f1b9ef7e000000000000000000000000e0a9e5b59701a776575fdd6257c3f89ae362629a0000000000000000000000000000000000000000000000000000000480920124";
//
//        Function function = new Function("transferFrom", new ArrayList<>(), Arrays.asList(TypeReference.create(Address.class), TypeReference.create(Address.class), new TypeReference<Uint256>() {
//        }));
//
//        List<Type> list = FunctionReturnDecoder.decode(input, function.getOutputParameters());
//
//        for (Type type : list) {
//            System.out.println(type.getValue().toString());
//        }


//        BscScanAccountTransaction bscScanAccountTransaction = new BscScanAccountTransaction();
//        bscScanAccountTransaction.setMethodId("0x40c10f19");
//        bscScanAccountTransaction.setInput("0x40c10f190000000000000000000000004f914ee31cb44d04c3ce8fccd6573d9af36c87800000000000000000000000000000000000000000000000000000000000000001");
//        bscScanAccountTransaction.setFunctionName("mint(address _owner, uint256 _amount)");
//
//        BscScanAccountTransaction(bscScanAccountTransaction);
//
//        String address = Keys.getAddress("0xa22cb465");
//        System.out.println(address);

//        BscScanTransactionLogDTO bscScanTransactionLogDTO = new BscScanTransactionLogDTO();
//        bscScanTransactionLogDTO.setTopics(Arrays.asList(
//                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
//                "0x0000000000000000000000000000000000000000000000000000000000000000",
//                "0x000000000000000000000000ff19c7cd7e5a1255ae5a8c850d93edf43303d347",
//                "0x0000000000000000000000000000000000000000000000000000000000006fbb"
//        ));
//
//        String s = BscScanLogTransaction(bscScanTransactionLogDTO);
//        System.out.println(s);

        Long aLong = Long.valueOf("83495f",16);
        System.out.println(aLong);
    }
}
