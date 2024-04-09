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
public class AdminProductController {
    private final AdminProductServiceImpl adminProductServiceImpl;

    public AdminProductController(AdminProductServiceImpl adminProductService) {
        this.adminProductServiceImpl = adminProductService;
    }

    @Value("img")
    private String IMAGE_DIR;

    @Value("C:/hedgehog/")
    private String ROOT_LOCATION;

    @PostMapping("/productRegist")
    public String producUpdate(@ModelAttribute AdminProductDTO product,
                               @RequestParam("thumbnail") MultipartFile thumbnail,
                               @RequestParam("sub_thumbnail") List<MultipartFile> sub_thumbnails,
                               @RequestParam("proImg") MultipartFile proImg,
                               RedirectAttributes rttr) {
        String rootLocation = ROOT_LOCATION + IMAGE_DIR;

        String fileUploadDirectory = rootLocation + "/upload/original";
        String thumnailDirectory = rootLocation + "/upload/thumbnail";

        File directory = new File(fileUploadDirectory);
        File directory2 = new File(thumnailDirectory);

        List<Map<String, String>> fileList = new ArrayList<>();

        List<MultipartFile> paramFileList = new ArrayList<>();
        paramFileList.add(thumbnail);
        for (int i = 0; i < sub_thumbnails.size(); i++) {
            paramFileList.add(sub_thumbnails.get(i));
        }
        paramFileList.add(proImg);
        try {
            for (MultipartFile paramFile : paramFileList) {
                if (paramFile.getSize() > 0) {
                    String originFileName = paramFile.getOriginalFilename();

                    String ext = originFileName.substring(originFileName.lastIndexOf("."));
                    String savedFileName = UUID.randomUUID().toString().replace("-", "") + ext;

                    paramFile.transferTo(new File(fileUploadDirectory + "/" + savedFileName));

                    Map<String, String> fileMap = new HashMap<>();
                    fileMap.put("originFileName", originFileName);
                    fileMap.put("savedFileName", savedFileName);
                    fileMap.put("savePath", fileUploadDirectory);

                    int width = 0;
                    int height = 0;

                    String fieldName = paramFile.getName();
                    if ("thumbnail".equals(fieldName)) {
                        fileMap.put("fileType", "Thumbnails");
                        width = 640;
                        height = 640;
                    } else if ("sub_thumbnail".equals(fieldName)) {
                        int subThumbnailIndex = sub_thumbnails.indexOf(paramFile);
                        if (subThumbnailIndex == 0) {
                            fileMap.put("fileType", "sub_thumbnail_1");
                            width = 640;
                            height = 640;
                        } else if (subThumbnailIndex == 1) {
                            fileMap.put("fileType", "sub_thumbnail_2");
                            width = 640;
                            height = 640;
                        } else if (subThumbnailIndex == 2) {
                            fileMap.put("fileType", "sub_thumbnail_3");
                            width = 640;
                            height = 640;
                        } else {
                            fileMap.put("fileType", "sub_thumbnail");
                            width = 640;
                            height = 640;
                        }
                    } else if ("proImg".equals(fieldName)) {
                        fileMap.put("fileType", "proImg");
                        width = 860;
                        height = 7500;
                    }

                    Thumbnails.of(fileUploadDirectory + "/" + savedFileName).size(width, height)
                            .toFile(thumnailDirectory + "/thumbnail_" + savedFileName);

                    fileMap.put("thumbnailPath", "/thumbnail_" + savedFileName);

                    fileList.add(fileMap);
                }
            }

            product.setAttachment(new ArrayList<AttachmentDTO>());
            List<AttachmentDTO> list = product.getAttachment();
            for (int i = 0; i < fileList.size(); i++) {
                Map<String, String> file = fileList.get(i);

                AttachmentDTO tempFileInfo = new AttachmentDTO();
                tempFileInfo.setOriginalName(file.get("originFileName"));
                tempFileInfo.setSavedName(file.get("savedFileName"));
                tempFileInfo.setSavePath(file.get("savePath"));
                tempFileInfo.setFileType(file.get("fileType"));
                tempFileInfo.setThumbnailPath(file.get("thumbnailPath"));

                list.add(tempFileInfo);
            }
            adminProductServiceImpl.productUpdate(product);
        } catch (IOException e) {
            int cnt = 0;
            for (int i = 0; i < fileList.size(); i++) {
                Map<String, String> file = fileList.get(i);
                File deleteFile = new File(fileUploadDirectory + "/" + file.get("savedFileName"));
                boolean isDeleted1 = deleteFile.delete();

                File deleteThumbnail = new File(thumnailDirectory + "/thumbnail_" + file.get("savedFileName"));
                boolean isDeleted2 = deleteThumbnail.delete();

                if (isDeleted1 && isDeleted2) {
                    cnt++;
                }
            }
        } catch (AdminProductAddException e) {
            e.printStackTrace();
        }

        rttr.addFlashAttribute("success", true);
        int productCode = product.getProductCode();
        return "redirect:/product/productDetail?productCode=" + productCode;
    }

    @GetMapping("/productDetail")
    public String selectProductDetail(@RequestParam int productCode, Model model) {
        AdminProductDTO product = adminProductServiceImpl.selectProductDetail(productCode);

        model.addAttribute("product", product);

        return "admin/content/product/productModify";
    }

    @PostMapping("/productAdd")
    private String productAdd(@ModelAttribute AdminProductDTO product,
                              @RequestParam("thumbnail") MultipartFile thumbnail,
                              @RequestParam("sub_thumbnail") List<MultipartFile> sub_thumbnails,
                              @RequestParam("proImg") MultipartFile proImg,
                              RedirectAttributes rttr) throws UnsupportedEncodingException, ThumbnailRegistException {
        String rootLocation = ROOT_LOCATION + IMAGE_DIR;

        String fileUploadDirectory = rootLocation + "/upload/original";
        String thumnailDirectory = rootLocation + "/upload/thumbnail";

        File directory = new File(fileUploadDirectory);
        File directory2 = new File(thumnailDirectory);

        List<Map<String, String>> fileList = new ArrayList<>();
        List<MultipartFile> paramFileList = new ArrayList<>();

        paramFileList.add(thumbnail);
        for (int i = 0; i < sub_thumbnails.size(); i++) {
            paramFileList.add(sub_thumbnails.get(i));
        }
        paramFileList.add(proImg);
        try {
            for (MultipartFile paramFile : paramFileList) {
                if (paramFile.getSize() > 0) {
                    String originFileName = paramFile.getOriginalFilename();

                    String ext = originFileName.substring(originFileName.lastIndexOf("."));
                    String savedFileName = UUID.randomUUID().toString().replace("-", "") + ext;

                    paramFile.transferTo(new File(fileUploadDirectory + "/" + savedFileName));

                    Map<String, String> fileMap = new HashMap<>();
                    fileMap.put("originFileName", originFileName);
                    fileMap.put("savedFileName", savedFileName);
                    fileMap.put("savePath", fileUploadDirectory);

                    int width = 0;
                    int height = 0;

                    String fieldName = paramFile.getName();
                    if ("thumbnail".equals(fieldName)) { //이름의 따라서 이미지 크기 변경 및 fileMap 객체에 새로운 이름으로 지정
                        fileMap.put("fileType", "Thumbnails");
                        width = 640;
                        height = 640;
                    } else if ("sub_thumbnail".equals(fieldName)) {
                        int subThumbnailIndex = sub_thumbnails.indexOf(paramFile);
                        if (subThumbnailIndex == 0) {
                            fileMap.put("fileType", "sub_thumbnail_1");
                            width = 640;
                            height = 640;
                        } else if (subThumbnailIndex == 1) {
                            fileMap.put("fileType", "sub_thumbnail_2");
                            width = 640;
                            height = 640;
                        } else if (subThumbnailIndex == 2) {
                            fileMap.put("fileType", "sub_thumbnail_3");
                            width = 640;
                            height = 640;
                        } else {
                            fileMap.put("fileType", "sub_thumbnail");
                            width = 640;
                            height = 640;
                        }
                    } else if ("proImg".equals(fieldName)) {
                        fileMap.put("fileType", "proImg");
                        width = 860;
                        height = 7500;
                    }

                    Thumbnails.of(fileUploadDirectory + "/" + savedFileName).size(width, height)
                            .toFile(thumnailDirectory + "/thumbnail_" + savedFileName);

                    fileMap.put("thumbnailPath", "/thumbnail_" + savedFileName);

                    fileList.add(fileMap);
                }
            }

            product.setAttachment(new ArrayList<AttachmentDTO>());
            List<AttachmentDTO> list = product.getAttachment();
            for (int i = 0; i < fileList.size(); i++) {
                Map<String, String> file = fileList.get(i);

                AttachmentDTO tempFileInfo = new AttachmentDTO();
                tempFileInfo.setOriginalName(file.get("originFileName"));
                tempFileInfo.setSavedName(file.get("savedFileName"));
                tempFileInfo.setSavePath(file.get("savePath"));
                tempFileInfo.setFileType(file.get("fileType"));
                tempFileInfo.setThumbnailPath(file.get("thumbnailPath"));

                list.add(tempFileInfo);
            }

            adminProductServiceImpl.productAdd(product);

            rttr.addFlashAttribute("success", true);
        } catch (IOException | AdminProductAddException e) {
            int cnt = 0;
            for (int i = 0; i < fileList.size(); i++) {
                Map<String, String> file = fileList.get(i);
                File deleteFile = new File(fileUploadDirectory + "/" + file.get("savedFileName"));
                boolean isDeleted1 = deleteFile.delete();

                File deleteThumbnail = new File(thumnailDirectory + "/thumbnail_" + file.get("savedFileName"));
                boolean isDeleted2 = deleteThumbnail.delete();

                if (isDeleted1 && isDeleted2) {
                    cnt++;
                }
            }
        }

        return "redirect:productAddPage";
    }

    @GetMapping(value = "/productserach")
    public ModelAndView productsearch(@ModelAttribute AdminProductForm form) {
        List<AdminProductDTO> productList = adminProductServiceImpl.searchProduct(form);

        int totalResult = productList.size();
        int countY = 0;
        int countN = 0;
        for (int i = 0; i < productList.size(); i++) {
            String orderableStatus = productList.get(i).getOrderableStatus();

            if (orderableStatus.equals("Y")) {
                countY++;
            }
            if (orderableStatus.equals("N")) {
                countN++;
            }
        }

        ModelAndView mv = new ModelAndView();

        mv.addObject("productList", productList);
        mv.addObject("totalResult", totalResult);
        mv.addObject("countY", countY);
        mv.addObject("countN", countN);
        mv.setViewName("admin/content/product/productserch");

        return mv;
    }

    @GetMapping("/productAddPage")
    public String productAddPage() {
        return "admin/content/product/productAdd";
    }

    @GetMapping(value = "/category/{upperCategoryCode}", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public List<AdminCategoryDTO> getCateogoryList(HttpServletResponse res, @PathVariable("upperCategoryCode") int upperCategoryCode) throws IOException {
        List<AdminCategoryDTO> categoryList = adminProductServiceImpl.findCategoryList(upperCategoryCode);

        return categoryList;
    }
}
