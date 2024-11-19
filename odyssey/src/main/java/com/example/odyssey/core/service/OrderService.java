package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;
import com.example.odyssey.bean.dto.OrderAppealDTO;
import com.example.odyssey.bean.dto.OrderDTO;

public interface OrderService {

    SingleResponse createOrder(OrderCreateCmd orderCreateCmd);

    SingleResponse cancelOrder(OrderCancelCmd orderCancelCmd);

    SingleResponse examineOrder(OrderExamineCmd orderExamineCmd);

    SingleResponse orderEmailAuth(OrderEmailAuthCmd orderEmailAuthCmd);

    SingleResponse appealOrder(OrderAppealCreateCmd orderAppealCreateCmd);

    SingleResponse cancelAppealOrder(OrderAppealCancelCmd orderAppealCancelCmd);

    SingleResponse handleAppealOrder(OrderAppealHandleCmd orderAppealHandleCmd);

    SingleResponse finishOrder(OrderFinishCmd orderFinishCmd);

    MultiResponse<OrderDTO> listOrder(OrderListQryCmd orderListQryCmd);

    MultiResponse<OrderAppealDTO> listOrderAppeal(OrderAppealListQryCmd orderAppealListQryCmd);
}
