package com.hedgehog.client.board.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.board.model.dto.UploadedImageDTO;
import com.hedgehog.client.board.model.dto.UploadedImageListDTO;
import com.hedgehog.client.board.model.service.BoardWriteService;
import com.hedgehog.client.orderDetails.model.dto.OrderDetailsCollect;
import com.hedgehog.client.orderDetails.model.dto.OrderDetailsDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
@RequestMapping("/board/*")
public class BoardWriteController {
    private final BoardWriteService boardWriteService;
    private final ObjectMapper objectMapper;

    public BoardWriteController(BoardWriteService boardWriteService, ObjectMapper objectMapper) {
        this.boardWriteService = boardWriteService;
        this.objectMapper = objectMapper;
    }

    @Value("img")
    private String IMAGE_DIR;

    @Value("C:/hedgehog/")
    private String ROOT_LOCATION;

    @GetMapping("/writeQuestion")
    public ModelAndView writeQuestion(ModelAndView mv) {
        mv.setViewName("/client/content/board/writeQuestion");
        return mv;
    }

    @PostMapping("/writeQuestion")
    public String questionRegist(@AuthenticationPrincipal LoginDetails loginDetails,
                                 @RequestParam String option,
                                 @RequestParam(required = false) String orderNumber,
                                 @RequestParam(required = false) String productName,
                                 @RequestParam String inputTitle,
                                 @RequestParam String editordata,
                                 @RequestParam String uploadedImages,
                                 RedirectAttributes redirectAttributes) {
        int userCode = loginDetails.getLoginUserDTO().getUserCode();
        String newEditorData = "<p>주문번호 : " + orderNumber + "</p><br><p>제품이름 : " + productName + "</p><br>" + editordata;
        try {
            List<UploadedImageDTO> uploadedImageList = null;
            if (!"".equals(uploadedImages)) {
                uploadedImageList = objectMapper.readValue(uploadedImages, new TypeReference<>() {
                });
            }
            boolean isSuccess = boardWriteService.questionRegist(userCode, option, inputTitle, newEditorData, uploadedImageList);

            if (!isSuccess) {
                redirectAttributes.addFlashAttribute("message", "알 수 없는 오류가 발생했습니다. 메인화면으로 나갑니다.");
                return "redirect:/";
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "알 수 없는 오류가 발생했습니다. 메인화면으로 나갑니다.");
            return "redirect:/";
        }
        return "redirect:/board/questionList";
    }

    @PostMapping(value = "/uploadSummernoteImageFile", produces = "application/json")
    @ResponseBody
    public Map<String, String> uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {
        String rootLocation = ROOT_LOCATION + IMAGE_DIR;

        String fileUploadDirectory = rootLocation + "/upload/original";
        String thumnailDirectory = rootLocation + "/upload/thumbnail";

        File directory = new File(fileUploadDirectory);
        File directory2 = new File(thumnailDirectory);

        Map<String, String> returnMap = new HashMap<>();
        String originalFileName = multipartFile.getOriginalFilename(); // source_name에 저장됨
        String ext = originalFileName.substring(originalFileName.lastIndexOf(".")); // source_name 에서 확장자를 가져옴
        String convertName = UUID.randomUUID().toString().replace("-", "") + ext; // convert_name. 새롭게 만든 파일이름
        File originalFile = new File(fileUploadDirectory + "/" + convertName);

        String convertPath = "/thumbnail_" + convertName;
        File convertFile = new File(thumnailDirectory + convertPath);

        try {
            InputStream fileStream = multipartFile.getInputStream();
            FileUtils.copyInputStreamToFile(fileStream, originalFile);

            int maxWidth = 640;
            int maxHeight = 640;

            Thumbnails.Builder<File> thumbnailBuilder = Thumbnails.of(originalFile);

            int originalWidth = (int) ImageIO.read(originalFile).getWidth();
            int originalHeight = (int) ImageIO.read(originalFile).getHeight();

            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                thumbnailBuilder.size(originalWidth, originalHeight);
            } else {
                thumbnailBuilder.size(maxWidth, maxHeight).keepAspectRatio(true);
            }

            thumbnailBuilder
                    .toFile(convertFile);

            returnMap.put("convertPath", convertPath);
            returnMap.put("savePath", fileUploadDirectory);
            returnMap.put("sourceName", originalFileName);
            returnMap.put("convertName", convertName);
            returnMap.put("url", "/thumbPath" + convertPath);
            returnMap.put("responseCode", "success");
        } catch (IOException e) {
            FileUtils.deleteQuietly(originalFile);
            FileUtils.deleteQuietly(convertFile);
            e.printStackTrace();
        }

        return returnMap;
    }

    @GetMapping("/writeReview")
    public String writeReview(@AuthenticationPrincipal LoginDetails loginDetails,
                              @RequestParam int orderDetailsCode,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (loginDetails == null) {
            redirectAttributes.addFlashAttribute("message", "잘못된 접근입니다. 메인으로 돌아갑니다.");
            return ("redirect:/");
        }
        String userId = loginDetails.getUsername();
        String myId = boardWriteService.findMyIdByOrderDetailsCode(orderDetailsCode);
        if (!myId.equals(userId)) {
            redirectAttributes.addFlashAttribute("message", "리뷰하려는 제품상세와 계정정보가 일치하지 않습니다. \n메인으로 돌아갑니다.");
            return ("redirect:/");
        }

        OrderDetailsDTO orderDetailsDTO = boardWriteService.selectOrderDetail(orderDetailsCode);
        model.addAttribute("orderDetailsDTO", orderDetailsDTO);
        return "/client/content/board/writeReview";
    }

    @PostMapping("/writeReview")
    public String reviewRegist(@AuthenticationPrincipal LoginDetails loginDetails,
                               @RequestParam String editordata,
                               @RequestParam String uploadedImages,
                               @RequestParam String orderDetailsCode,
                               @RequestParam String stars,
                               RedirectAttributes redirectAttributes) {
        int userCode = loginDetails.getLoginUserDTO().getUserCode();
        try {
            List<UploadedImageDTO> uploadedImageList = null;
            if (!"".equals(uploadedImages)) {
                uploadedImageList = objectMapper.readValue(uploadedImages, new TypeReference<>() {
                });
            }
            String userId = loginDetails.getUsername();
            String myId = boardWriteService.findMyIdByOrderDetailsCode(Integer.parseInt(orderDetailsCode));
            if (!myId.equals(userId)) {
                redirectAttributes.addFlashAttribute("message", "리뷰하려는 제품상세와 계정정보가 일치하지 않습니다. \n메인으로 돌아갑니다.");
                return ("redirect:/");
            }

            OrderDetailsDTO orderDetailsDTO = boardWriteService.selectOrderDetail(Integer.parseInt(orderDetailsCode));
            boolean isSuccess = boardWriteService.reviewRegist(userCode, editordata, orderDetailsDTO, stars, uploadedImageList);

            if (!isSuccess) {
                redirectAttributes.addFlashAttribute("message", "알 수 없는 오류가 발생했습니다. 메인화면으로 나갑니다.");
                return "redirect:/";
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "알 수 없는 오류가 발생했습니다. 메인화면으로 나갑니다.");
            return "redirect:/";
        }
        return "redirect:/board/reviewList";
    }
}
