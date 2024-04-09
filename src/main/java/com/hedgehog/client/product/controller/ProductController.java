package com.hedgehog.client.product.controller;

import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.board.model.dto.ProductReviewDTO;
import com.hedgehog.client.product.model.dto.ProductDetailDTO;
import com.hedgehog.client.product.model.dto.ProductDetailReviewDTO;
import com.hedgehog.client.product.model.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Controller
@RequestMapping("/productinfo")
public class ProductController {

    @Value("${image.image-dir}")
    private String IMAGE_DIR;

    @Value("${spring.servlet.multipart.location}")
    private String ROOT_LOCATION;

    private final ProductService productService;

    public ProductController(ProductService productService) {

        this.productService = productService;
    }

    @GetMapping("/product/{number}")
    public String selectProductDetail(@PathVariable int number, Model model) {
        List<ProductDetailDTO> productDetail = productService.selectProductDetail(number);

        long productPrice = Long.parseLong(productDetail.get(0).getProductText().get(0).getProductPrice());  /* 가격 확인 */

        Long productPriceObject = Long.valueOf(productPrice);

        Set<String> overlapOptionName = new HashSet<>();
        for (ProductDetailDTO detail : productDetail) {
            overlapOptionName.add(detail.getProductText().get(0).getOptionName());
        }

        List<String> optionNameList = new ArrayList<>(overlapOptionName);

        List<ProductDetailReviewDTO> productDetailReview = productService.selectProductReview(number);

        String star = "fa-solid fa-star";
        String halfStar = "fa-solid fa-star-half";

        List<String> result = new ArrayList<>();

        double reviewAvg = 0.0;
        if (productDetailReview.get(0) != null) {
            reviewAvg = productDetailReview.get(0).getReviewAvg();

            int integerPart = (int) reviewAvg;
            if (integerPart > 0) {

                for (int i = 0; i < integerPart; i++) {
                    result.add(star);
                }

                if (reviewAvg - integerPart > 0) {
                    result.add(halfStar);
                }

            }
            System.out.println("reviewAvg======" + reviewAvg);
        }

        model.addAttribute("productDetail", productDetail);
        model.addAttribute("productPrice", productPrice);
        model.addAttribute("productOption", optionNameList);
        model.addAttribute("productReview", productDetailReview);
        model.addAttribute("result", result);

        return "client/content/productinfo/product";
    }

    @PostMapping("/addcart")
    public String addcart(@RequestParam String optionColor,
                          @RequestParam int productCode,
                          @AuthenticationPrincipal LoginDetails loginDetails,
                          Model model) {

        int userCode = loginDetails.getLoginUserDTO().getUserCode();
        productService.addCart(optionColor, productCode, userCode);

        return "redirect:/basket/cart";
    }
}
