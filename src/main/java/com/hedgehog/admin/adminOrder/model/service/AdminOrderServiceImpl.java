package com.hedgehog.admin.adminOrder.model.service;

import com.hedgehog.admin.adminOrder.model.dao.AdminOrderMapper;
import com.hedgehog.admin.adminOrder.model.dto.AdminOrderDTO;
import com.hedgehog.admin.adminOrder.model.dto.AdminOrderForm;
import com.hedgehog.admin.adminProduct.model.dto.AdminProductDTO;
import com.hedgehog.admin.exception.OrderStateUpdateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminOrderServiceImpl implements AdminOrderService{
    private final AdminOrderMapper mapper;

    public AdminOrderServiceImpl(AdminOrderMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminOrderDTO> searchOrderList(AdminOrderForm form) {
        List<AdminOrderDTO> orderList = mapper.searchOrderList(form);
        return orderList;
    }

    @Override
    @Transactional
    public void orderStateUpdate(AdminOrderDTO orderDTO) throws OrderStateUpdateException {
        int result = 0;
        int result2 =0;
        if("4".equals(orderDTO.getState())) {
            mapper.orderStateUpdate(orderDTO);
            result++;
            mapper.paymentTableUpdate(orderDTO);
            result++;
            mapper.refundTableUpdate(orderDTO);
            result++;

            mapper.deliverTableUpdate(orderDTO);
            result++;
        } else if ("5".equals(orderDTO.getState())){
            mapper.orderStateUpdate(orderDTO);
            result++;
            mapper.paymentTableUpdate(orderDTO);
            result++;
            mapper.exchangeTableUpdate(orderDTO);
            result++;
            mapper.deliverTableUpdate(orderDTO);
            result++;
        } else {
            mapper.orderStateUpdate(orderDTO);
            result2++;
            mapper.deliveryStateUpdate(orderDTO);
            result2++;
            mapper.paymentTableUpdate(orderDTO);
            result2++;
        }
        if(!(result > 3 || result2 > 1)) {
            throw new OrderStateUpdateException("상태 변경에 실패하셨습니다.");
        }
    }

    @Override
    public AdminOrderDTO orderDetail(int orderCode) {
        AdminOrderDTO orderDTO = null;

        orderDTO = mapper.orderDetail(orderCode);

        return orderDTO;
    }

    @Override
    @Transactional
    public void exchange(AdminOrderDTO orderDTO) throws OrderStateUpdateException {
        int result1 = mapper.orderStateUpdate(orderDTO);
        int result2 = mapper.deliverTableUpdate(orderDTO);
        int result3 = mapper.paymentTableUpdate(orderDTO);
        int result4 = mapper.exchangeTableInsert(orderDTO);
        if(!(result1 > 0 ||result2 > 0 || result3 > 0 ||result4 > 0)) {
            throw new OrderStateUpdateException("상태 변경에 실패하셨습니다.");
        }
    }

    @Override
    @Transactional
    public void refund(AdminOrderDTO orderDTO) throws OrderStateUpdateException {
        int result1 = mapper.orderStateUpdate(orderDTO);
        int result2 = mapper.deliverTableUpdate(orderDTO);
        int result3 = mapper.paymentTableUpdate(orderDTO);
        int result4 = mapper.refundTableInsert(orderDTO);
        if(!(result1 > 0 ||result2 > 0 || result3 > 0 ||result4 > 0)) {
            throw new OrderStateUpdateException("상태 변경에 실패하셨습니다.");
        }
    }


}
