package com.hedgehog.client.orderDetails.model.service;

import com.hedgehog.client.orderDetails.model.dao.OrderDetailsMapper;
import com.hedgehog.client.orderDetails.model.dto.OrderDTO;
import com.hedgehog.client.orderDetails.model.dto.OrderDetailsCollect;
import com.hedgehog.client.orderDetails.model.dto.OrderListDTO;
import com.hedgehog.common.paging.orderDetailsPaging.OrderDetailsSelectCriteria;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderDetailsService {
    private final OrderDetailsMapper mapper;

    public int selectTotalCountOrderInfo(int userCode, OrderDTO order, String info) {
        /*
         * info에는 두가지 경우가 있다.
         * "orderDeliveryInfo"
         * "exchangePaybackInfo"
         * 둘의 쿼리문이 큰 차이가 없기 때문에 이와 같이 설정했다.(조건문의 차이)
         * */
        int result = mapper.selectTotalCountOrderInfo(userCode, order, info);

        return result;
    }

    public List<OrderListDTO> selectOrderInfoList(int userCode, OrderDetailsSelectCriteria orderDetailsSelectCriteria, String info) {
        List<OrderListDTO> result = mapper.selectOrderInfoList(userCode, orderDetailsSelectCriteria, info);

        return result;
    }


    public boolean isYourOrder(int userCode, int orderCode) {
        int result = mapper.isYourOrder(userCode, orderCode);
        return result == 1;
    }

    public OrderDetailsCollect getOrderDetails(int orderCode) {
        OrderDetailsCollect orderDetailsCollect = mapper.getOrderDetails(orderCode);
        return orderDetailsCollect;
    }

    public Integer selectOrderCode(Integer orderCode, String email) {
        Integer newOrderCode = mapper.selectOrderCode(orderCode, email);
        return newOrderCode;
    }

    public int selectUserCode(Integer orderCode) {
        return mapper.selectUserCode(orderCode);
    }

    @Transactional
    public boolean updateReceiveOrder(String orderCode) {
        String result1 = mapper.selectReceiveOrder(orderCode);
        int result2 = 0;
        if (result1.equals("배송중")) {
            result2 = mapper.updateReceiveOrder(orderCode);
            String deliveryCode = mapper.selectDeliveryCode(orderCode);
            mapper.updateDelivery(deliveryCode);
        }
        return result2 >= 0 ? true : false;
    }
}
