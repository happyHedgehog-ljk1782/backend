package com.hedgehog.admin.adminService.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgehog.admin.adminOrder.model.dto.AdminOrderDTO;
import com.hedgehog.admin.adminService.model.dao.AdminAutoMapper;
import com.hedgehog.admin.adminService.model.dto.AdminAutoMailDTO;
import com.hedgehog.admin.adminService.model.dto.AdminAutoMailForm;
import com.hedgehog.admin.adminService.model.service.AdminAutoMailServiceImpl;
import com.hedgehog.admin.exception.AdminProductAddException;
import com.hedgehog.client.board.model.dto.UploadedImageDTO;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/autoMailModify")
public class AutoMailController {
    private final AdminAutoMailServiceImpl autoMail;
    private final ObjectMapper objectMapper;

    public AutoMailController(AdminAutoMailServiceImpl autoMail, ObjectMapper objectMapper) {
        this.autoMail = autoMail;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/emailDetail")
    public String emailDetail(@RequestParam int mailCode, Model model) {
        AdminAutoMailDTO mailDTO = autoMail.emailDetail(mailCode);

        model.addAttribute("mailDTO", mailDTO);
        return "admin/content/Service/emailDetail";
    }

    @GetMapping(value = "/searchEmailHistory")
    public ModelAndView searchEmailHistory(@ModelAttribute AdminAutoMailForm form) {
        List<AdminAutoMailDTO> mailDTOList = autoMail.searchEmailHistory(form);

        int totalResult = mailDTOList.size();

        ModelAndView mv = new ModelAndView();
        mv.addObject("mailList", mailDTOList);
        mv.setViewName("admin/content/Service/emailHistory");

        return mv;
    }

    @Value("img")
    private String IMAGE_DIR;

    @Value("C:/hedgehog/")
    private String ROOT_LOCATION;

    @PostMapping(value = "/mainSend")
    public String mainSend(@RequestParam String uploadedImages,
                           @RequestParam String title,
                           @RequestParam String summernote,
                           @RequestParam String chooseMember, RedirectAttributes rttr) throws JsonProcessingException, MessagingException, UnsupportedEncodingException {

        if (uploadedImages != null && !uploadedImages.isEmpty()) {
            List<UploadedImageDTO> uploadedImageList = objectMapper.readValue(uploadedImages, new TypeReference<List<UploadedImageDTO>>() {
            });

            boolean isSucces = autoMail.sendMail(uploadedImageList, title, summernote, chooseMember);

            return "redirect:/Service/email";
        } else {
            boolean isSucces = autoMail.sendMailOnlyString(title, summernote, chooseMember);

            return "redirect:/Service/email";
        }
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

    @GetMapping("/previewMail")
    public String previewMail(@RequestParam int mailCode, Model model) {
        AdminAutoMailDTO mailDTO = autoMail.previewMail(mailCode);

        model.addAttribute(mailDTO);

        return "admin/content/Service/mailViewport";
    }

    @GetMapping("/modifyMailPage")
    public String modifyMailPage(@RequestParam int mailCode, Model model) {
        AdminAutoMailDTO mailDTO = autoMail.previewMail(mailCode);

        model.addAttribute(mailDTO);

        return "admin/content/Service/mailModify";
    }

    @PostMapping("/modifyMail")
    public String modifyMail(@ModelAttribute AdminAutoMailDTO mailDTO,
                             RedirectAttributes rttr) throws AdminProductAddException {
        autoMail.modifyMail(mailDTO);

        rttr.addFlashAttribute("message", "양식 변경에 성공하였습니다.");

        return "redirect:/autoMailModify/modifyMailPage?mailCode=" + mailDTO.getFormCode();
    }
}
