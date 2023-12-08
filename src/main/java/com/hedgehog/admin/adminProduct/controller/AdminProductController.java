package com.hedgehog.admin.adminProduct.controller;
import com.hedgehog.admin.adminProduct.model.dto.*;
import com.hedgehog.admin.adminProduct.model.service.AdminProductServiceImpl;
import com.hedgehog.admin.exception.AdminProductAddException;
import com.hedgehog.admin.exception.ThumbnailRegistException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Controller
@RequestMapping("/product")
@Slf4j
public class AdminProductController {

    private final AdminProductServiceImpl adminProductServiceImpl;

    public AdminProductController(AdminProductServiceImpl adminProductService) {
        this.adminProductServiceImpl = adminProductService;
    }

//    @PostMapping("/productRegist")
//    public String productRegist(@ModelAttribute AdminProductDTO product,
//                                RedirectAttributes rttr){
//
//        log.info("***************************상품 수정 시작");
//
//        adminProductServiceImpl.registProduct(product);
//
//        rttr.addFlashAttribute("message", "상품 수정에 성공하셨습니다.")
//        return "redirect:/product/productModify";
//    }

    @GetMapping("/productDetail")
    public String selectProductDetail(@RequestParam int productCode, Model model){
        log.info("");
        log.info("");
        log.info("selectProductDetail~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~시작");
        log.info("~~~~~~~~~~~~~~~~product : {}", productCode);

        AdminProductDTO product = adminProductServiceImpl.selectProductDetail(productCode);
        log.info("=====================================product : " + product);
        model.addAttribute("product", product);

        return "admin/content/product/productModify";



    }

    @Value("img")
    private String IMAGE_DIR;

    @Value("C:/images/")
    private String ROOT_LOCATION;

    /**
     * 상품 등록 메소드
     * @param product 상품 DTO
     * @param optionDTO 옵션 DTO
     * @param option 옵션 List DTO
     * @param category 카테고리 DTO
     * @param thumbnail 대표썸네일
     * @param sub_thumbnails 제품관점이미지
     * @param proImg 제품 상세페이지 이미지
     * @param rttr 성공 실패시 메세지
     * @return
     * @throws UnsupportedEncodingException
     * @throws ThumbnailRegistException
     */
    @PostMapping("/productAdd")
    private String productAdd(@ModelAttribute AdminProductDTO product,
                              @ModelAttribute OptionDTO optionDTO,
                              @ModelAttribute OptionListDTO option,
                              @ModelAttribute AdminCategoryDTO category,
                              @RequestParam("thumbnail") MultipartFile thumbnail,
                              @RequestParam("sub_thumbnail") List<MultipartFile> sub_thumbnails,
                              @RequestParam("proImg") MultipartFile proImg,
                              RedirectAttributes rttr) throws UnsupportedEncodingException, ThumbnailRegistException {

        log.info("********************=============productAdd 시작~~~~~~~~~");
        log.info("==========product" + product);
        log.info("===========optionList" + option);
        log.info("=============option" + optionDTO);
        log.info("================adminCategory" + category);
        log.info("==========thumbnail" + thumbnail);
        log.info("==========sub_thumbnail" + sub_thumbnails);
        log.info("==========proImg" + proImg);


        product.setOptionDTO(optionDTO);
        product.setCategory(category);
        product.setOption(option);


        log.info("=================사진 등록 시작~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        String rootLocation = ROOT_LOCATION + IMAGE_DIR;

        String fileUploadDirectory = rootLocation + "/upload/original";
        String thumnailDirectory = rootLocation + "/upload/thumbnail";

        File directory = new File(fileUploadDirectory);
        File directory2 = new File(thumnailDirectory);

        log.info("~~~~~~~~~~~~~~~~~~~~~fileUploadDirectory" + fileUploadDirectory);
        log.info("****************************thumnailDirectory" + thumnailDirectory);

        if(!directory.exists() || !directory2.exists()){
            log.info("*************************** 폴더 생성" + directory.mkdirs());
            log.info("*************************** 폴더 생성2" + directory2.mkdirs());
        }

        List<Map<String, String>> fileList = new ArrayList<>();

        List<MultipartFile> paramFileList = new ArrayList<>();
        paramFileList.add(thumbnail);
        log.info("=======================thumbnail" + thumbnail);
        for (int i = 0; i < sub_thumbnails.size(); i++){
            paramFileList.add(sub_thumbnails.get(i));
            log.info("=====================sub_thumbnails" + sub_thumbnails.get(i));
        }
        paramFileList.add(proImg);
        log.info("============proImg" + proImg);
        try {
        for(MultipartFile paramFile : paramFileList) {
            if (paramFile.getSize() > 0) {
                String originFileName = paramFile.getOriginalFilename();

                log.info("~~~~~~~~~~~~~~~~~~~~~originFileName" + originFileName);

                String ext = originFileName.substring(originFileName.lastIndexOf("."));
                String savedFileName = UUID.randomUUID().toString().replace("-", "") + ext;

                log.info("++++++++++++++++++변경한 이름" + savedFileName);

                log.info("+++++++++++++++ paramFile : " + fileUploadDirectory + "/" + savedFileName);

                    paramFile.transferTo(new File(fileUploadDirectory + "/" + savedFileName));


                Map<String, String> fileMap = new HashMap<>();
                fileMap.put("originFileName", originFileName);
                fileMap.put("savedFileName", savedFileName);
                fileMap.put("savePath", fileUploadDirectory);

                int width = 0;
                int height = 0;

                String fieldName = paramFile.getName();
                log.info("***********************필드 name {} ", fieldName);
                log.info("============================= 확인 {} ", ("proImg").equals(fieldName));
                if ("thumbnail".equals(fieldName)) {
                    fileMap.put("fileType", "Thumbnails");

                    width = 640;
                    height = 640;
                } else if("sub_thumbnail".equals(fieldName)) {
                    fileMap.put("fileType", "sub_thumbnail");
                    width = 640;
                    height = 640;
                } else if("proImg".equals(fieldName)){
                    fileMap.put("fileType", "proImg");
                    width = 860;
                    height = 7500;
                }

                Thumbnails.of(fileUploadDirectory + "/" + savedFileName).size(width, height)
                        .toFile(thumnailDirectory + "/thumbnail" + savedFileName);

                fileMap.put("thumbnailPath", "/thumbnail_" + savedFileName);

                fileList.add(fileMap);
            }
        }

        log.info("****************************fileList" + fileList);

        product.setAttachment(new ArrayList<AttachmentDTO>());
        List<AttachmentDTO> list = product.getAttachment();
        for(int i = 0; i < fileList.size(); i++){
            Map<String , String > file = fileList.get(i);

            AttachmentDTO tempFileInfo = new AttachmentDTO();
            tempFileInfo.setOriginalName(file.get("originFileName"));
            tempFileInfo.setSavedName(file.get("savedFileName"));
            tempFileInfo.setSavePath(file.get("savePath"));
            tempFileInfo.setFileType(file.get("fileType"));
            tempFileInfo.setThumbnailPath(file.get("thumbnailPath"));

            list.add(tempFileInfo);

        }

        log.info("------------------thumbnail" + thumbnail);


            OptionDTO optionCode = adminProductServiceImpl.searchOption(optionDTO);
            log.info("****************조회한 optionCode" + optionCode);
            log.info("=============다른 DTO 추가한 product" + product);


            if(optionCode != null && optionCode.getOptionCode() != optionDTO.getOptionCode()) {
                log.info("*************************중복 옵션 코드 있을때 실행하는 메소드~~~~~~~~~~~");
                adminProductServiceImpl.productAddExcludeOptionCode(product);
            } else if(optionCode == null) {
                log.info("-------------------------중복 옵션 코드 없을때 실행하는 메소드~~~~~~~~~~~");
                adminProductServiceImpl.productAdd(product);
            }







        rttr.addFlashAttribute("message", "상품 등록에 성공하였습니다.");
        } catch (IOException | AdminProductAddException e) {
            e.printStackTrace();


        int cnt = 0;
        for(int i = 0; i < fileList.size(); i++) {
            Map<String, String> file = fileList.get(i);
            File deleteFile = new File(fileUploadDirectory + "/" + file.get("savedFileName"));
            boolean isDeleted1 = deleteFile.delete();

            File deleteThumbnail = new File(thumnailDirectory + "/thumbnail_" + file.get("savedFileName"));
            boolean isDeleted2 = deleteThumbnail.delete();

            if (isDeleted1 && isDeleted2) {
                cnt++;
            }

            if (cnt == fileList.size()) {
                log.info("*******************업로드 실패한 사진 삭제~~~~~~~~~");
                e.printStackTrace();
            } else {
                e.printStackTrace();
            }
        }
        }
        log.info("=============product 끗~~~~~~~~~~~~~~~");
        return "redirect:productAddPage";
    }






    /**
     * 상품 조회하는 메소드
     * @param form html에서 form 데이터로 전달받은 객체를 선언한 DTO
     * @return 조회된 리스트, 총 상품수, 판매중인 상품수, 판매중지 상태인 상품수를 반환
     */
    @GetMapping(value = "/productserach")
    public ModelAndView productsearch(@ModelAttribute AdminProductForm form) {
        log.info("productsearch ====================== start");

        log.info(form.toString());

        List<AdminProductDTO> productList = adminProductServiceImpl.searchProduct(form);
        log.info("=================================productList" + productList);

        int totalResult = productList.size();
        int countY = 0;
        int countN = 0;
        for (int i = 0; i < productList.size(); i++) {
            String orderableStatus = productList.get(i).getOrderableStatus();
            log.info(orderableStatus);

            if (orderableStatus.equals("Y")) {
                countY++;

            }
            if(orderableStatus.equals("N")){
                countN++;
            }

        }
        log.info("=============================countY" + countY);
        log.info("=============================countN" + countN);

        ModelAndView modelAndView = new ModelAndView("admin/content/product/productSerch");
        modelAndView.addObject("productList", productList); // 모델에 productList를 추가
        modelAndView.addObject("totalResult", totalResult);
        modelAndView.addObject("countY", countY);
        modelAndView.addObject("countN", countN);



        log.info("totalResult" + String.valueOf(totalResult));

        return modelAndView;
    }



    /**
     * 상품조회 페이지 연결 메소드
     * @return 관리자 상품조회 페이지
     */
//    @GetMapping("/productserachPage")
//    public String productsearch(){ return "admin/content/product/productSerch";}


    @GetMapping("/productAddPage")
    public String productadd(){ return "admin/content/product/productAdd";}
    /**
     * ajax 이용 동적 select 메소드
     * @return 선택한 상위 카테고리의 하위 카테고리 리스트들
     */
    @GetMapping(value = "/category/{upperCategoryCode}", produces = "application/json; charset=UTF-8" )
    @ResponseBody
    public List<AdminCategoryDTO> getCateogoryList(HttpServletResponse res, @PathVariable("upperCategoryCode") int upperCategoryCode) throws IOException {
        log.info("*************************" + upperCategoryCode);

        List<AdminCategoryDTO> categoryList = adminProductServiceImpl.findOptionList(upperCategoryCode);
        log.info("******************" + categoryList);


        return categoryList;
    }





}
