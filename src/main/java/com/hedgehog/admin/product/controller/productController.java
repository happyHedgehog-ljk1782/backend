package com.hedgehog.admin.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class productController {

    @GetMapping("/search")
    public String productsearch(){ return "admin/content/product/productSerch.html";}


    @GetMapping("/add")
    public String productadd(){ return "admin/content/product/productAdd.html";}



}
