package com.hedgehog.client.product.model.service;

import com.hedgehog.client.product.model.dao.ProductInfoMapper;
import com.hedgehog.client.product.model.dto.ProductDetailDTO;
import com.hedgehog.client.product.model.dto.ProductDetailReviewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductInfoMapper mapper;

    public ProductServiceImpl(ProductInfoMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ProductDetailDTO> selectProductDetail(int number) {

        List<ProductDetailDTO> result = mapper.selectProductDetail(number);

        return result;
    }

    @Override
    public List<ProductDetailReviewDTO> selectProductReview(int number) {

        List<ProductDetailReviewDTO> result = mapper.selectProductReview(number);

        System.out.println("리뷰===="+result);

        return result;
    }

    @Transactional
    @Override
    public void addCart(String color, int productCode, int userCode) {
        String colorCode = mapper.getColorCode(productCode,color);
        mapper.addCart(colorCode, productCode, userCode);
    }
}
