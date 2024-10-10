package com.example.odyssey.util;

import cn.hutool.json.JSONUtil;
import com.example.odyssey.bean.dto.TransferFromDTO;
import com.example.odyssey.common.FunctionTypeClassEnum;
import com.example.odyssey.model.entity.BscScanTransaction;
import org.springframework.util.StringUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

import java.util.*;


public class InputDataDecoderUtil {

    public static void BscScanTransaction(BscScanTransaction bscScanTransaction) {

        if (!StringUtils.hasLength(bscScanTransaction.getFunctionName())) {
            return;
        }

        try {
            // 去除方法签名
            String input = bscScanTransaction.getInput().substring(bscScanTransaction.getMethodId().length());

            // 解析方法 获取 参数类型
            String functionName = bscScanTransaction.getFunctionName();

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

            Function function = new Function(bscScanTransaction.getFunctionName(), new ArrayList<>(), outputParameters);

            List<Type> list = FunctionReturnDecoder.decode(input, function.getOutputParameters());

            for (int i = 0; i < list.size(); i++) {

                TransferFromDTO transferFromDTO = transferFromList.get(i);
                transferFromDTO.setData(list.get(i).getValue().toString());
            }

            bscScanTransaction.setDecodedInput(JSONUtil.toJsonStr(transferFromList));
        }catch (Exception e){
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


        BscScanTransaction bscScanTransaction = new BscScanTransaction();
        bscScanTransaction.setMethodId("0x40c10f19");
        bscScanTransaction.setInput("0x40c10f190000000000000000000000004f914ee31cb44d04c3ce8fccd6573d9af36c87800000000000000000000000000000000000000000000000000000000000000001");
        bscScanTransaction.setFunctionName("mint(address _owner, uint256 _amount)");

        BscScanTransaction(bscScanTransaction);
//
//        String address = Keys.getAddress("0xa22cb465");
//        System.out.println(address);
    }
}
