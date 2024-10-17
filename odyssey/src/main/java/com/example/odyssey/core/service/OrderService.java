package com.example.odyssey.core.service;

import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.*;

public interface OrderService {

    SingleResponse createOrder(OrderCreateCmd orderCreateCmd);

    SingleResponse cancelOrder(OrderCancelCmd orderCancelCmd);

    SingleResponse examineOrder(OrderExamineCmd orderExamineCmd);

    SingleResponse appealOrder(OrderAppealCreateCmd orderAppealCreateCmd);

    SingleResponse cancelAppealOrder(OrderAppealCancelCmd orderAppealCancelCmd);

    SingleResponse handleAppealOrder(OrderAppealHandleCmd orderAppealHandleCmd);

    SingleResponse finishOrder(OrderFinishCmd orderFinishCmd);
}
