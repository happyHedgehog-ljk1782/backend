package com.hedgehog.admin.adminProduct.model.service;

import com.hedgehog.admin.adminProduct.model.dao.AdminProductMapper;
import com.hedgehog.admin.adminProduct.model.dto.*;
import com.hedgehog.admin.exception.AdminProductAddException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.List;

@Service
public class AdminProductServiceImpl implements AdminProductService {
    private final AdminProductMapper mapper;

    public AdminProductServiceImpl(AdminProductMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminCategoryDTO> categoryList(AdminCategoryDTO category) {
        List<AdminCategoryDTO> categoryList = mapper.searchCategoryList(category);
        return categoryList;
    }

    @Override
    public List<AdminProductDTO> categoryDetail(String categoryCode) {
        if ("1".equals(categoryCode)) {
            Integer.parseInt(categoryCode);
            List<AdminProductDTO> productDTO = mapper.searchUpperCategoryDetail(categoryCode);
            productDTO.get(0).getCategory().setName("침실");

            return productDTO;

        } else if ("2".equals(categoryCode)) {
            Integer.parseInt(categoryCode);

            List<AdminProductDTO> productDTO = mapper.searchUpperCategoryDetail(categoryCode);
            productDTO.get(0).getCategory().setName("거실");

            return productDTO;
        } else if ("3".equals(categoryCode)) {
            Integer.parseInt(categoryCode);

            List<AdminProductDTO> productDTO = mapper.searchUpperCategoryDetail(categoryCode);
            productDTO.get(0).getCategory().setName("서재");

            return productDTO;
        } else if ("4".equals(categoryCode)) {
            Integer.parseInt(categoryCode);

            List<AdminProductDTO> productDTO = mapper.searchUpperCategoryDetail(categoryCode);
            productDTO.get(0).getCategory().setName("주방");

            return productDTO;

        } else {
            List<AdminProductDTO> productDTO = mapper.searchCategoryDetail(categoryCode);
            return productDTO;
        }
    }

    @Override
    @Transactional
    public void categoryModify(AdminCategoryForm categoryForm) throws AdminProductAddException {
        Integer.parseInt(categoryForm.getUpperCategoryCode());
        Integer.parseInt(categoryForm.getSubCategoryName());


        int result = mapper.categoryModify(categoryForm);

        if (!(result > 0)) {

            throw new AdminProductAddException("카테고리 수정에 실패하셨습니다.");
        }
    }

    @Override
    public List<AdminProductDTO> searchProduct(AdminProductForm form) {
        List<AdminProductDTO> productList = mapper.searchProduct(form);

        return productList;
    }

    @Override
    @Transactional
    public void productAdd(AdminProductDTO product) throws AdminProductAddException {
        int addProduct = mapper.addProduct(product);
        int addOptionResult = 0;

        for (int i = 0; i < product.getOptionDTO().size(); i++) {
            OptionDTO optionDTO = product.getOptionDTO().get(i);
            int addOption = mapper.addOption(optionDTO);
            addOptionResult += addOption;
        }

        int productCode = product.getProductCode();
        List<OptionListDTO> optionListDTO = product.getOptionList();

        int addOptionListResult = 0;

        for (int i = 0; i < product.getOptionDTO().size(); i++) {
            optionListDTO.get(i).setProductCode(productCode);
            optionListDTO.get(i).setOptionCode(product.getOptionDTO().get(i).getOptionCode());
            optionListDTO.get(i).setStock(product.getOptionList().get(i).getStock());
            int addOptionList = mapper.addOptionList(optionListDTO.get(i));
            addOptionListResult += addOptionList;

        }
        List<AttachmentDTO> attachmentList = product.getAttachment();

        for (int i = 0; i < attachmentList.size(); i++) {
            attachmentList.get(i).setProductCode(product.getProductCode());
        }

        int attachmentResult = 0;
        for (int i = 0; i < attachmentList.size(); i++) {
            attachmentResult += mapper.addImg(attachmentList.get(i));
        }

        if (!(addProduct > 0) && !(addOptionResult > 0) && !(attachmentResult > 0) && !(addOptionListResult > 0)) {
            throw new AdminProductAddException("상품 등록에 실패하셨습니다.");
        }
    }

    @Override
    public List<AdminCategoryDTO> findCategoryList(int upperCategoryCode) {
        List<AdminCategoryDTO> findCategory = mapper.searchCategory(upperCategoryCode);
        return findCategory;
    }

    @Override
    public AdminProductDTO selectProductDetail(int productCode) {
        AdminProductDTO productDTO = null;
        productDTO = mapper.selectProductDetail(productCode);

        return productDTO;
    }

    @Override
    @Transactional
    public void productUpdate(AdminProductDTO product) throws AdminProductAddException {
        int updateProduct = mapper.productUpdate(product);

        int updateOption = 0;
        for (int i = 0; i < product.getOptionDTO().size(); i++) {
            OptionDTO optionDTO = product.getOptionDTO().get(i);
            optionDTO.setOptionCode(product.getOptionList().get(i).getOptionCode());
            int addOption = mapper.addOption(optionDTO);
            updateOption += addOption;
        }
        List<OptionListDTO> optionListDTO = product.getOptionList();

        for (int i = 0; i < optionListDTO.size(); i++) {
            optionListDTO.get(i).setProductCode(product.getProductCode());

            int addOptionList = mapper.addOptionList2(optionListDTO.get(i));
        }

        for (AttachmentDTO attachment : product.getAttachment()) {
            attachment.setProductCode(product.getProductCode());
            if (!attachment.getOriginalName().isEmpty()) {
                int updateImg = mapper.updateImg(attachment);
            }
        }

        if (!(updateProduct > 0) || !(updateOption > 0)) {
            throw new AdminProductAddException("상품 수정에 실패하였습니다.");
        }
    }
}
