package com.hedgehog.client.list.controller;

import com.hedgehog.client.list.dto.ProductListDTO;
import com.hedgehog.client.list.service.ProductListService;
import com.hedgehog.common.paging.Pagenation;
import com.hedgehog.common.paging.SelectCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/list")
public class ListController {

    @Value("${image.image-dir}")
    private String IMAGE_DIR;

    @Value("${spring.servlet.multipart.location}")
    private String ROOT_LOCATION;

    private final ProductListService productListService;

    public ListController(ProductListService productListService) {
        this.productListService = productListService;
    }

    @GetMapping("/productList/{type}")
    public ModelAndView selectProductList(ModelAndView mv,
                                          @PathVariable String type,
                                          @RequestParam(value = "currentPage", defaultValue = "1") int pageNo) {
        int totalCount = productListService.selectTotalPageCount(type);

        int limit = 12;
        int buttonAmount = 5;
        SelectCriteria selectCriteria = null;
        selectCriteria = Pagenation.getSelectCriteria(pageNo, totalCount, limit, buttonAmount);

        Map<String, Object> map = new HashMap<>();
        map.put("selectCriteria", selectCriteria);
        map.put("type", type);
        List<ProductListDTO> productList = productListService.selectProductList(map);  /* 상품리스트 */

        mv.addObject("productList", productList);
        mv.addObject("map", map);
        mv.addObject("selectCriteria", selectCriteria);

        int discount = (int) productList.get(0).getDiscount();

        mv.setViewName("client/content/list/productList");

        return mv;
    }
}
