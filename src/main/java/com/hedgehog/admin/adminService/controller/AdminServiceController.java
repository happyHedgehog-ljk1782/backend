package com.hedgehog.admin.adminService.controller;

import com.hedgehog.admin.adminService.model.dto.*;
import com.hedgehog.admin.adminService.model.service.AdminCommentServiceImpl;
import com.hedgehog.admin.adminService.model.service.AdminFAQServiceImpl;
import com.hedgehog.admin.adminService.model.service.AdminInquiryServiceImpl;
import com.hedgehog.admin.adminService.model.service.AdminReviewServiceImpl;
import com.hedgehog.admin.exception.BoardException;
import com.hedgehog.client.product.model.service.ProductService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
@RequestMapping("/Service")
public class AdminServiceController {
    private final AdminInquiryServiceImpl adminInquiryServiceImpl;
    private final AdminFAQServiceImpl adminFAQServiceImpl;
    private final AdminReviewServiceImpl adminReviewServiceImpl;
    private final AdminCommentServiceImpl adminCommentServiceImpl;

    public AdminServiceController(AdminInquiryServiceImpl adminInquiryServiceImpl, AdminReviewServiceImpl adminReviewServiceImpl, AdminFAQServiceImpl adminFAQServiceImpl, AdminCommentServiceImpl adminCommentServiceImpl) {
        this.adminInquiryServiceImpl = adminInquiryServiceImpl;
        this.adminReviewServiceImpl = adminReviewServiceImpl;
        this.adminFAQServiceImpl = adminFAQServiceImpl;
        this.adminCommentServiceImpl = adminCommentServiceImpl;
    }

    @GetMapping("/productInquiryPage")
    public String productInquiryPage() {
        return "admin/content/Service/Product-inquiry";
    }

    @GetMapping(value = "/productInquiry")
    public ModelAndView productInquiry(@ModelAttribute AdminInquiryForm form) {
        List<AdminInquiryDTO> inquiryList = adminInquiryServiceImpl.searchInquiry(form);

        int totalResult = inquiryList.size(); //전체 결과 수, 답변 및 미답변 수 계산
        int countY = 0;
        int countN = 0;
        for (int i = 0; i < inquiryList.size(); i++) {
            String answer_state = inquiryList.get(i).getAnswer_state();

            if (answer_state.equals("Y")) {
                countY++;
            }
            if (answer_state.equals("N")) {
                countN++;
            }
        }

        ModelAndView modelAndView = new ModelAndView("admin/content/Service/Product-inquiry");
        modelAndView.addObject("inquiryList", inquiryList); //상품 문의 목록을 모델에 추가
        modelAndView.addObject("totalResult", totalResult); //전체 결과 수를 모델에 추가
        modelAndView.addObject("countY", countY); // 답변완료 문의 수를 모델에 추가
        modelAndView.addObject("countN", countN); // 미답변 문의 수를 모델에 추가

        return modelAndView;
    }

    @GetMapping("/inquiryDetail")
    public String InquiryDetail(@RequestParam("inquiry_code") int inquiry_code,
                                @RequestParam("answer_state") String answer_state,
                                Model model) {
        AdminInquiryDTO adminInquiryDTO = adminInquiryServiceImpl.inquiryDetail(inquiry_code);

        model.addAttribute("adminInquiryDTO", adminInquiryDTO);
        return "admin/content/Service/Product-inquiry-details";
    }

    @PostMapping(value = "/inqStateUpdate")
    private String inqStateUpdate(@RequestParam("resultCheckbox") List<String> selectedInqCodes, // 체크박스로 선택된 상품문의코드 받기
                                  @RequestParam("inqSelectCommit") String selectedInqState,
                                  RedirectAttributes rttr) throws BoardException {
        for (int i = 0; i < selectedInqCodes.size(); i++) {
            if ("on".equals(selectedInqCodes.get(i)) || selectedInqCodes.get(i).isEmpty()) {
                continue;
            } else {
                int inquiryCode = Integer.parseInt(selectedInqCodes.get(i));
                AdminInquiryDTO inquiryDTO = new AdminInquiryDTO();
                inquiryDTO.setInquiry_code(inquiryCode);
                inquiryDTO.setState(selectedInqState);
                adminInquiryServiceImpl.inqStateUpdate(inquiryDTO);
            }
        }
        rttr.addFlashAttribute("success", true);
        return "redirect:/Service/productInquiryPage";
    }

    @PostMapping("/inquiryComment")
    public String inquiryComment(@ModelAttribute AdminCommentDTO adminCommentDTO,
                                 @RequestParam("inquiry_code") int inquiry_code,
                                 @RequestParam("user_code") int user_code,
                                 @RequestParam("inqtitle") String inqtitle,
                                 @RequestParam("inqcontent") String inqcontent,
                                 Model model) throws BoardException, MessagingException, UnsupportedEncodingException {
        adminCommentDTO.setInquiry_code(inquiry_code);
        adminCommentDTO.setUser_code(user_code);
        adminCommentDTO.setInqtitle(inqtitle);
        adminCommentDTO.setInqcontent(inqcontent);

        adminCommentServiceImpl.inquiryComment(adminCommentDTO);
        model.addAttribute("comment_code", adminCommentDTO.getComment_code());
        model.addAttribute("inquiry_code", inquiry_code);

        return "admin/content/Service/blank";
    }

    @GetMapping("/Product-reviewPage")
    public String productReview() {
        return "admin/content/Service/Product-review";
    }

    @GetMapping("/Product-review")
    public ModelAndView productReview(@ModelAttribute AdminReviewForm form) {
        List<AdminReviewDTO> reviewList = adminReviewServiceImpl.searchReview(form);

        int totalResult = reviewList.size();

        ModelAndView modelAndView = new ModelAndView("admin/content/Service/Product-review");
        modelAndView.addObject("reviewList", reviewList);
        modelAndView.addObject("totalResult", totalResult);

        return modelAndView;
    }

    @GetMapping("/reviewDetail")
    public String reviewDetail(@RequestParam("Review_code") int Review_code, Model model) {
        AdminReviewDTO adminReviewDTO = adminReviewServiceImpl.reviewDetail(Review_code);
        model.addAttribute("adminReviewDTO", adminReviewDTO);

        return "admin/content/Service/Product-review-details";
    }

    @Autowired
    private ProductService productService;

    @PostMapping(value = "/revStateUpdate")
    private String revStateUpdate(@RequestParam("resultCheckbox") List<String> selectedRevCodes,
                                  @RequestParam("revSelectCommit") String selectedRevState,
                                  RedirectAttributes rttr) throws BoardException {
        for (int i = 0; i < selectedRevCodes.size(); i++) {
            if ("on".equals(selectedRevCodes.get(i)) || selectedRevCodes.get(i).isEmpty()) {
                continue;
            } else {
                int reviewCode = Integer.parseInt(selectedRevCodes.get(i));
                AdminReviewDTO reviewDTO = new AdminReviewDTO();
                reviewDTO.setReview_code(reviewCode);
                reviewDTO.setState(selectedRevState);

                adminReviewServiceImpl.revStateUpdate(reviewDTO);
            }
        }
        rttr.addFlashAttribute("message", "상태가 변경되었습니다.");
        return "redirect:/Service/Product-review";
    }

    @GetMapping("/FAQPage")
    public String FAQ() {
        return "admin/content/Service/FAQ";
    }

    @GetMapping("/FAQ")
    public ModelAndView FAQ(@ModelAttribute AdminFAQForm form) {
        List<AdminFAQDTO> FAQList = adminFAQServiceImpl.searchFAQ(form);

        int totalResult = FAQList.size();

        ModelAndView modelAndView = new ModelAndView("admin/content/Service/FAQ");
        modelAndView.addObject("FAQList", FAQList);
        modelAndView.addObject("totalResult", totalResult);

        return modelAndView;
    }

    @PostMapping(value = "/FAQStateUpdate")
    private String FAQStateUpdate(@RequestParam("resultCheckbox") List<String> selectedFAQCodes,
                                  @RequestParam("FAQSelectCommit") String selectedFAQState,
                                  RedirectAttributes rttr) throws BoardException {
        for (int i = 0; i < selectedFAQCodes.size(); i++) {
            if ("on".equals(selectedFAQCodes.get(i)) || selectedFAQCodes.get(i).isEmpty()) {
                continue;
            } else {
                int FAQCode = Integer.parseInt(selectedFAQCodes.get(i));
                AdminFAQDTO FAQDTO = new AdminFAQDTO();
                FAQDTO.setPost_code(FAQCode);
                FAQDTO.setState(selectedFAQState);

                adminFAQServiceImpl.FAQStateUpdate(FAQDTO);
            }
        }
        rttr.addFlashAttribute("message", "상태가 변경되었습니다.");
        return "redirect:/Service/FAQ";
    }

    @PostMapping("/FAQRegister")
    public String FAQRegister(@ModelAttribute AdminFAQDTO adminFAQDTO,
                              Model model) throws BoardException {
        adminFAQServiceImpl.FAQRegister(adminFAQDTO);
        model.addAttribute(adminFAQDTO.getPost_code());

        return "admin/content/Service/FAQ";
    }

    @PostMapping(value = "/FAQModify")
    public String FAQModify(@ModelAttribute AdminFAQDTO adminFAQDTO,
                            Model model) {
        adminFAQServiceImpl.FAQModify(adminFAQDTO);
        return "admin/content/Service/FAQ";
    }

    @GetMapping("/FAQModifyPage")
    public String FAQModifyPage(@RequestParam("postCode") int postCode, Model model) {
        AdminFAQDTO adminFAQDTO = adminFAQServiceImpl.FAQModifyPage(postCode);

        model.addAttribute("adminNoticeDTO", adminFAQDTO);

        return "admin/content/Service/FAQModify";
    }

    @GetMapping("/noticePage")
    public String notice() {
        return "admin/content/Service/notice";
    }

    @GetMapping(value = "/notice")
    public ModelAndView notice(@ModelAttribute AdminFAQForm form) {
        List<AdminFAQDTO> noticeList = adminFAQServiceImpl.searchNotice(form);

        int totalResult = noticeList.size();
        int countY = 0;
        int countN = 0;
        for (int i = 0; i < noticeList.size(); i++) {
            String state = noticeList.get(i).getState();

            if (state.equals("Y")) {
                countY++;
            }
            if (state.equals("N")) {
                countN++;
            }
        }

        ModelAndView modelAndView = new ModelAndView("admin/content/Service/notice");
        modelAndView.addObject("noticeList", noticeList);
        modelAndView.addObject("totalResult", totalResult);
        modelAndView.addObject("countY", countY);
        modelAndView.addObject("countN", countN);

        return modelAndView;
    }

    @PostMapping(value = "/noticeStateUpdate")
    private String noticeStateUpdate(@RequestParam("resultCheckbox") List<String> selectedNoticeCodes,
                                     @RequestParam("noticeSelectCommit") String selectedNoticeState,
                                     RedirectAttributes rttr) throws BoardException {
        for (int i = 0; i < selectedNoticeCodes.size(); i++) {
            if ("on".equals(selectedNoticeCodes.get(i)) || selectedNoticeCodes.get(i).isEmpty()) {
                continue;
            } else {
                int noticeCode = Integer.parseInt(selectedNoticeCodes.get(i));
                AdminFAQDTO FAQDTO = new AdminFAQDTO();
                FAQDTO.setPost_code(noticeCode);
                FAQDTO.setState(selectedNoticeState);

                adminFAQServiceImpl.noticeStateUpdate(FAQDTO);
            }
        }
        rttr.addFlashAttribute("message", "상태가 변경되었습니다.");
        return "redirect:/Service/notice";
    }

    @PostMapping("/noticeRegister")
    public String noticeRegister(@ModelAttribute AdminFAQDTO adminFAQDTO,
                                 Model model) throws BoardException {
        adminFAQServiceImpl.noticeRegister(adminFAQDTO);
        model.addAttribute(adminFAQDTO.getPost_code());

        return "admin/content/Service/notice";
    }

    @PostMapping(value = "/noticeModify")
    public String noticeModify(@ModelAttribute AdminFAQDTO adminFAQDTO,
                               Model model) {
        adminFAQServiceImpl.FAQModify(adminFAQDTO);
        return "admin/content/Service/notice";
    }

    @GetMapping("/noticeModifyPage")
    public String noticeModifyPage(@RequestParam("postCode") int postCode, Model model) {
        AdminFAQDTO adminFAQDTO = adminFAQServiceImpl.FAQModifyPage(postCode);

        model.addAttribute("adminNoticeDTO", adminFAQDTO);

        return "admin/content/Service/noticeModify";
    }

    @GetMapping("/FAQWritePage")
    public String FAQWritePage() {
        return "admin/content/Service/FAQWrite";
    }

    @GetMapping("/email")
    public String email() {
        return "admin/content/Service/email";
    }

    @GetMapping("/emailHistoryPage")
    public String emailHistory() {
        return "admin/content/Service/emailHistory";
    }

    @GetMapping("/autoMail")
    public String autoMail() {
        return "admin/content/Service/autoMail";
    }

    @GetMapping("/noticeWritePage")
    public String noticeWritePage() {
        return "admin/content/Service/noticeWrite";
    }

    @GetMapping("/detail")
    public String productInquiryDetail() {
        return "admin/content/Service/Product-inquiry-details";
    }


}






