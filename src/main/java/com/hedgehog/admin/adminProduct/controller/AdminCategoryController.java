package com.hedgehog.admin.adminProduct.controller;

import com.hedgehog.admin.adminProduct.model.dto.AdminCategoryDTO;
import com.hedgehog.admin.adminProduct.model.dto.AdminCategoryForm;
import com.hedgehog.admin.adminProduct.model.dto.AdminProductDTO;
import com.hedgehog.admin.adminProduct.model.dto.AdminProductForm;
import com.hedgehog.admin.adminProduct.model.service.AdminProductServiceImpl;
import com.hedgehog.admin.exception.AdminProductAddException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/category")
public class AdminCategoryController {
    private final AdminProductServiceImpl adminProductServiceImpl;

    public AdminCategoryController(AdminProductServiceImpl adminProductServiceImpl) {
        this.adminProductServiceImpl = adminProductServiceImpl;
    }

    @PostMapping(value = "categoryModify")
    public String categoryModify(@ModelAttribute AdminCategoryForm categoryForm,
                                 RedirectAttributes rttr) throws AdminProductAddException {
        adminProductServiceImpl.categoryModify(categoryForm);
        rttr.addFlashAttribute("success", true);

        return "redirect:/category/categoryAdd";
    }

    @GetMapping(value = "/categoryDetail", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public List<AdminProductDTO> handleCategoryClick(@RequestParam("categoryName") String categoryName) {
        categoryName.trim();

        List<AdminProductDTO> productDTO = adminProductServiceImpl.categoryDetail(categoryName);

        int totalCount = productDTO.size();
        int stateY = 0;
        int stateN = 0;
        for (int i = 0; i < productDTO.size(); i++) {
            if (productDTO.get(i).getOrderableStatus().equals("Y")) {
                stateY++;
                productDTO.get(i).setPrice(stateY);
                productDTO.get(i).setProductCode(totalCount);
            }else {
                stateN++;
                productDTO.get(i).setDeliveryCharge(stateN);
                productDTO.get(i).setProductCode(totalCount);

            }
        }
        productDTO.get(0).setOrderableStatus(productDTO.get(0).getCategory().getState());
        return productDTO;
    }

    @GetMapping(value = "/categoryAdd")
    public ModelAndView categoryadd(@ModelAttribute AdminCategoryDTO category,
                                    ModelAndView mv) {
        List<AdminCategoryDTO> categoryList = adminProductServiceImpl.categoryList(category);

        int totalResult = categoryList.size();

        mv.addObject("categoryList", categoryList);
        mv.addObject("totalResult", totalResult);
        mv.setViewName("admin/content/product/categoryAdd");

        return mv;
    }

    @GetMapping("/categoryPage")
    public String productAddPage() {
        return "admin/content/product/categoryAdd";
    }
}