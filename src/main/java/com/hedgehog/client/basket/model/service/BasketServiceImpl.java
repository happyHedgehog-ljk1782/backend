package com.hedgehog.client.basket.model.service;

import com.hedgehog.client.basket.model.dao.BasketMapper;
import com.hedgehog.client.basket.model.dto.CartSelectDTO;
import com.hedgehog.client.basket.model.dto.CartSumDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BasketServiceImpl implements BasketService {

    private final BasketMapper basketMapper;

    @Autowired
    public BasketServiceImpl(BasketMapper basketMapper) {
        this.basketMapper = basketMapper;
    }

    @Override
    public List<CartSelectDTO> selectCartList(int userCode) {
        return basketMapper.selectCartListByUserCode(userCode);
    }

    @Override
    public List<CartSumDTO> selectCartSum() {
            return basketMapper.selectCartSum();
    }

    @Override
    public int getTotalCartSum(List<CartSumDTO> cartSumDTOList) {
        int totalSum = 0;
        for (CartSumDTO cartItem : cartSumDTOList){
            int productSum = cartItem.getPrice() * cartItem.getAmount();
            totalSum += productSum;
        }
        return totalSum;
    }

    @Override
    public void deleteCartItems(List<Integer> cartCode) {
        basketMapper.deleteCartItems(cartCode);
    }
}
