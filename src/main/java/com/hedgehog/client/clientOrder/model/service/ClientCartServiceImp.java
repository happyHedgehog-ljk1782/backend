package com.hedgehog.client.clientOrder.model.service;

import com.hedgehog.client.basket.model.dto.CartSelectDTO;
import com.hedgehog.client.clientOrder.model.dao.CartOrderMapper;
import com.hedgehog.client.clientOrder.model.dto.ClientCartOrderForm;
import com.hedgehog.client.clientOrder.model.dto.OrderInfoDTO;
import com.hedgehog.client.kakaopay.model.dto.OrderPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ClientCartServiceImp implements ClientCartService {
    private final CartOrderMapper cartOrderMapper;

    @Autowired
    public ClientCartServiceImp(CartOrderMapper cartOrderMapper) {
        this.cartOrderMapper = cartOrderMapper;
    }

    @Override
    public List<CartSelectDTO> selectCartOrder(List<Integer> cartcheckbox) {
        return cartOrderMapper.cartOrderSelect(cartcheckbox);
    }

    @Override
    public int getOrderPoint(int userCode) {
        return cartOrderMapper.getOrderPoint(userCode);
    }

    @Override
    public OrderInfoDTO getOrderInfo(int userCode) {
        return cartOrderMapper.getOrderInfo(userCode);
    }

    @Override
    public int updateUserPoint(int userCode, int usedPoints) {
        return cartOrderMapper.updateUserPoint(userCode, usedPoints);
    }

    @Override
    public ClientCartOrderForm getUserOrder(int userCode) {
        return cartOrderMapper.getUserOrder(userCode);
    }
}
