package com.example.odyssey.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.OrderAppealDTO;
import com.example.odyssey.bean.dto.OrderDTO;
import com.example.odyssey.common.OrderAppealStatusEnum;
import com.example.odyssey.common.OrderStatusEnum;
import com.example.odyssey.core.service.EmailService;
import com.example.odyssey.core.service.OrderService;
import com.example.odyssey.model.entity.*;
import com.example.odyssey.model.mapper.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Resource
    OrderMapper orderMapper;
    @Resource
    RedissonClient redissonClient;
    @Resource
    OrderAppealMapper orderAppealMapper;
    @Resource
    EmailService emailService;
    @Resource
    NftMessageMapper nftMessageMapper;
    @Resource
    SystemConfigMapper systemConfigMapper;
    @Resource
    HotelMapper hotelMapper;

    @Override
    public SingleResponse createOrder(OrderCreateCmd orderCreateCmd) {

        RLock redLock = redissonClient.getLock("odyssey:create:order:" + orderCreateCmd.getTokenId());

        try {
            if (redLock.tryLock(5 * 1000, TimeUnit.MILLISECONDS)) {

                QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("token_id", orderCreateCmd.getTokenId());
                NftMessage nftMessage = nftMessageMapper.selectOne(queryWrapper);
                if (nftMessage == null) {
                    return SingleResponse.buildFailure("nft不存在");
                }

                if (nftMessage.getBlockadeTime() > System.currentTimeMillis()) {
                    return SingleResponse.buildFailure("nft已被封锁");
                }

                Hotel hotel = hotelMapper.selectById(orderCreateCmd.getHotelId());
                if (hotel == null) {
                    return SingleResponse.buildFailure("酒店不存在");
                }
                if (!hotel.getState().equals(nftMessage.getState())) {
                    return SingleResponse.buildFailure("酒店不可选择");
                }

                //todo 是否需要做订单重复判断

                Order order = new Order();
                BeanUtils.copyProperties(orderCreateCmd, order);
                order.setStatus(OrderStatusEnum.AUTHENTICATION.getCode());
                order.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                orderMapper.insert(order);

                //todo 发送邮件
                EmailCreateCmd emailCreateCmd = new EmailCreateCmd();
                emailCreateCmd.setEmail(orderCreateCmd.getEmail());
                emailCreateCmd.setOrderId(order.getId());

                emailService.sendEmail(emailCreateCmd);

                Integer blockadeDay = 90;

                QueryWrapper<SystemConfig> systemQueryWrapper = new QueryWrapper<>();

                systemQueryWrapper.eq("key", "nft_blockade_day");

                SystemConfig systemConfig = systemConfigMapper.selectOne(systemQueryWrapper);

                if (Objects.nonNull(systemConfig)) {
                    blockadeDay = Integer.valueOf(systemConfig.getValue());
                }

                nftMessage.setBlockadeTime(System.currentTimeMillis() + blockadeDay * 24 * 60 * 60 * 1000);

                nftMessageMapper.updateById(nftMessage);

                return SingleResponse.buildSuccess();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (redLock.isHeldByCurrentThread()) {
                redLock.unlock();
            }
        }
        return SingleResponse.buildFailure("创建订单失败");
    }

    @Override
    public SingleResponse cancelOrder(OrderCancelCmd orderCancelCmd) {

        RLock redLock = redissonClient.getLock("odyssey:order:" + orderCancelCmd.getOrderId());

        try {
            if (redLock.tryLock(5 * 1000, TimeUnit.MILLISECONDS)) {

                Order order = orderMapper.selectById(orderCancelCmd.getOrderId());
                if (order == null) {
                    return SingleResponse.buildFailure("订单不存在");
                }

                if (order.getStatus().equals(OrderStatusEnum.AUTHENTICATION.getCode()) || order.getStatus().equals(OrderStatusEnum.EXAMINE.getCode())) {

                    order.setReason(orderCancelCmd.getReason());
                    order.setCancelTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    order.setStatus(OrderStatusEnum.CANCEL.getCode());
                    orderMapper.updateById(order);

                    QueryWrapper<NftMessage> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("token_id", order.getTokenId());
                    NftMessage nftMessage = nftMessageMapper.selectOne(queryWrapper);

                    if (Objects.nonNull(nftMessage)) {
                        nftMessage.setBlockadeTime(0L);
                        nftMessageMapper.updateById(nftMessage);
                    }

                    return SingleResponse.buildSuccess();
                }

                return SingleResponse.buildFailure("订单状态不正确");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (redLock.isHeldByCurrentThread()) {
                redLock.unlock();
            }
        }
        return SingleResponse.buildFailure("取消订单失败");
    }

    @Override
    public SingleResponse examineOrder(OrderExamineCmd orderExamineCmd) {

        RLock redLock = redissonClient.getLock("odyssey:order:" + orderExamineCmd.getOrderId());

        try {
            if (redLock.tryLock(5 * 1000, TimeUnit.MILLISECONDS)) {

                Order order = orderMapper.selectById(orderExamineCmd.getOrderId());
                if (order == null) {
                    return SingleResponse.buildFailure("订单不存在");
                }

                if (order.getStatus() != OrderStatusEnum.EXAMINE.getCode()) {
                    return SingleResponse.buildFailure("订单状态不正确");
                }

                order.setExamineTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                order.setStatus(orderExamineCmd.getStatus());
                order.setReason(orderExamineCmd.getReason());

                orderMapper.updateById(order);

                return SingleResponse.buildSuccess();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (redLock.isHeldByCurrentThread()) {
                redLock.unlock();
            }
        }
        return SingleResponse.buildFailure("审核订单失败");
    }

    @Override
    public SingleResponse appealOrder(OrderAppealCreateCmd orderAppealCreateCmd) {

        OrderAppeal orderAppeal = new OrderAppeal();
        BeanUtils.copyProperties(orderAppealCreateCmd, orderAppeal);
        orderAppeal.setStatus(OrderAppealStatusEnum.PENDING.getCode());
        orderAppeal.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        orderAppealMapper.insert(orderAppeal);

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse cancelAppealOrder(OrderAppealCancelCmd orderAppealCancelCmd) {

        OrderAppeal orderAppeal = orderAppealMapper.selectById(orderAppealCancelCmd.getOrderAppealId());
        if (orderAppeal == null) {
            return SingleResponse.buildFailure("申诉订单不存在");
        }

        if (orderAppeal.getStatus() != OrderAppealStatusEnum.PENDING.getCode()) {
            return SingleResponse.buildFailure("申诉订单状态不正确");
        }

        orderAppeal.setStatus(OrderAppealStatusEnum.CANCEL.getCode());
        orderAppeal.setRemark(orderAppealCancelCmd.getRemark());
        orderAppealMapper.updateById(orderAppeal);

        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse handleAppealOrder(OrderAppealHandleCmd orderAppealHandleCmd) {

        OrderAppeal orderAppeal = orderAppealMapper.selectById(orderAppealHandleCmd.getOrderAppealId());

        if (orderAppeal == null) {
            return SingleResponse.buildFailure("申诉订单不存在");
        }

        if (orderAppeal.getStatus() != OrderAppealStatusEnum.PENDING.getCode()) {
            return SingleResponse.buildFailure("申诉订单状态不正确");
        }

        orderAppeal.setRemark(orderAppealHandleCmd.getRemark());
        orderAppeal.setStatus(OrderAppealStatusEnum.FINISH.getCode());
        orderAppeal.setFinishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        orderAppealMapper.updateById(orderAppeal);
        return SingleResponse.buildSuccess();
    }

    @Override
    public SingleResponse finishOrder(OrderFinishCmd orderFinishCmd) {

        Order order = orderMapper.selectById(orderFinishCmd.getOrderId());
        if (order == null) {
            return SingleResponse.buildFailure("订单不存在");
        }

        if (order.getStatus() != OrderStatusEnum.PASS.getCode()) {
            return SingleResponse.buildFailure("订单状态不正确");
        }

        order.setFinishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        order.setStatus(OrderStatusEnum.FINISH.getCode());

        orderMapper.updateById(order);

        return SingleResponse.buildSuccess();
    }

    @Override
    public MultiResponse<OrderDTO> listOrder(OrderListQryCmd orderListQryCmd) {

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(orderListQryCmd.getStatus())) {
            queryWrapper.eq("status", orderListQryCmd.getStatus());
        }
        if (Objects.nonNull(orderListQryCmd.getAddress())) {
            queryWrapper.eq("address", orderListQryCmd.getAddress());
        }
        if (Objects.nonNull(orderListQryCmd.getTokenId())) {
            queryWrapper.eq("token_id", orderListQryCmd.getTokenId());
        }
        if (Objects.nonNull(orderListQryCmd.getHotel())) {
            queryWrapper.like("hotel", orderListQryCmd.getHotel());
        }
        if (Objects.nonNull(orderListQryCmd.getHotelState())) {
            queryWrapper.eq("hotel_state", orderListQryCmd.getHotelState());
        }
        if (Objects.nonNull(orderListQryCmd.getHotelCity())) {
            queryWrapper.eq("hotel_city", orderListQryCmd.getHotelCity());
        }
        if (Objects.nonNull(orderListQryCmd.getHotelAddress())) {
            queryWrapper.like("hotel_address", orderListQryCmd.getHotelAddress());
        }
        if (Objects.nonNull(orderListQryCmd.getName())) {
            queryWrapper.like("name", orderListQryCmd.getName());
        }
        if (Objects.nonNull(orderListQryCmd.getPhone())) {
            queryWrapper.like("phone", orderListQryCmd.getPhone());
        }
        if (Objects.nonNull(orderListQryCmd.getCreateStartTime()) && Objects.nonNull(orderListQryCmd.getCreateEndTime())) {
            queryWrapper.between("create_time", orderListQryCmd.getCreateStartTime(), orderListQryCmd.getCreateEndTime());
        }
        if (Objects.nonNull(orderListQryCmd.getCancelStartTime()) && Objects.nonNull(orderListQryCmd.getCancelEndTime())) {
            queryWrapper.between("cancel_time", orderListQryCmd.getCancelStartTime(), orderListQryCmd.getCancelEndTime());
        }
        if (Objects.nonNull(orderListQryCmd.getFinishStartTime()) && Objects.nonNull(orderListQryCmd.getFinishEndTime())) {
            queryWrapper.between("finish_time", orderListQryCmd.getFinishStartTime(), orderListQryCmd.getFinishEndTime());
        }
        if (Objects.nonNull(orderListQryCmd.getExamineStartTime()) && Objects.nonNull(orderListQryCmd.getExamineEndTime())) {
            queryWrapper.between("examine_time", orderListQryCmd.getExamineStartTime(), orderListQryCmd.getExamineEndTime());
        }


        Page<Order> orderPage = orderMapper.selectPage(Page.of(orderListQryCmd.getPageNum(), orderListQryCmd.getPageSize()), queryWrapper);

        if (orderPage.getRecords().isEmpty()) {
            return MultiResponse.buildSuccess();
        }

        List<Integer> hotelIds = orderPage.getRecords().stream().map(Order::getHotelId).collect(Collectors.toList());

        Map<Integer, Hotel> hotelMap = hotelMapper.selectBatchIds(hotelIds).stream().collect(Collectors.toMap(Hotel::getId, hotel -> hotel));


        List<OrderDTO> orderDTOList = new ArrayList<>();

        for (Order order : orderPage.getRecords()) {
            OrderDTO orderDTO = new OrderDTO();
            BeanUtils.copyProperties(order, orderDTO);

            Hotel hotel = hotelMap.get(order.getHotelId());
            if (Objects.nonNull(hotel)) {
                orderDTO.setHotelName(hotel.getName());
                orderDTO.setHotelPhone(hotel.getPhone());
                orderDTO.setHotelEmail(hotel.getEmail());
                orderDTO.setHotelState(hotel.getState());
                orderDTO.setHotelCity(hotel.getCity());
                orderDTO.setHotelAddress(hotel.getAddress());
            }
            orderDTOList.add(orderDTO);
        }

        return MultiResponse.of(orderDTOList, (int) orderPage.getTotal());
    }

    @Override
    public MultiResponse<OrderAppealDTO> listOrderAppeal(OrderAppealListQryCmd orderAppealListQryCmd) {

        QueryWrapper<OrderAppeal> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(orderAppealListQryCmd.getOrderId())) {
            queryWrapper.eq("order_id", orderAppealListQryCmd.getOrderId());
        }
        if (Objects.nonNull(orderAppealListQryCmd.getStatus())) {
            queryWrapper.eq("status", orderAppealListQryCmd.getStatus());
        }
        if (Objects.nonNull(orderAppealListQryCmd.getCreateStartTime()) && Objects.nonNull(orderAppealListQryCmd.getCreateEndTime())) {
            queryWrapper.between("create_time", orderAppealListQryCmd.getCreateStartTime(), orderAppealListQryCmd.getCreateEndTime());
        }
        if (Objects.nonNull(orderAppealListQryCmd.getFinishStartTime()) && Objects.nonNull(orderAppealListQryCmd.getFinishEndTime())) {
            queryWrapper.between("finish_time", orderAppealListQryCmd.getFinishStartTime(), orderAppealListQryCmd.getFinishEndTime());
        }
        queryWrapper.orderByDesc("id");
        Page<OrderAppeal> orderAppealPage = orderAppealMapper.selectPage(Page.of(orderAppealListQryCmd.getPageNum(), orderAppealListQryCmd.getPageSize()), queryWrapper);

        List<OrderAppealDTO> orderAppealDTOList = new ArrayList<>();

        for (OrderAppeal orderAppeal : orderAppealPage.getRecords()){

            OrderAppealDTO orderAppealDTO = new OrderAppealDTO();
            BeanUtils.copyProperties(orderAppeal,orderAppealDTO);

            orderAppealDTOList.add(orderAppealDTO);
        }

        return MultiResponse.of(orderAppealDTOList, (int) orderAppealPage.getTotal());
    }
}
