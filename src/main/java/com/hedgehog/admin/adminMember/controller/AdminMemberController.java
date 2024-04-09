package com.hedgehog.admin.adminMember.controller;

import com.hedgehog.admin.adminMember.model.dto.*;
import com.hedgehog.admin.adminMember.model.service.AdminMemberServiceImpl;
import com.hedgehog.admin.adminOrder.model.dto.AdminOrderDTO;
import com.hedgehog.admin.adminService.model.dto.AdminAutoMailDTO;
import com.hedgehog.admin.exception.OrderStateUpdateException;
import com.hedgehog.admin.exception.UnregistException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/member")
public class AdminMemberController {
    private final AdminMemberServiceImpl adminMemberServiceimpl;

    public AdminMemberController(AdminMemberServiceImpl adminMemberService) {
        this.adminMemberServiceimpl = adminMemberService;
    }

    @PostMapping(value = "/selectMemberSendMailPage")
    private ModelAndView selectMemberSendMailPage(@RequestParam("resultCheckbox") List<String> memberCode) {
        AdminSendMailDTO sendMailDTO = adminMemberServiceimpl.selectMemberSendMailPage(7);

        sendMailDTO.setMemberId(new ArrayList<>(memberCode));

        ModelAndView mv = new ModelAndView();
        mv.addObject("sendMailDTO", sendMailDTO);
        mv.setViewName("admin/content/member/sendMail");

        return mv;
    }

    @PostMapping(value = "/sendMail")
    private String sendMail(@ModelAttribute AdminSendMailDTO mailDTO,
                            RedirectAttributes rttr) throws UnregistException {
        adminMemberServiceimpl.sendMail(mailDTO);
        rttr.addFlashAttribute("message", "메일 전송이 성공하였습니다.");

        return "admin/content/member/blank";
    }


    @GetMapping(value = "/pointAdd")
    private String pointAdd(@RequestParam("memberCode") int memberCode,
                            @RequestParam("point") int point) throws UnregistException {
        AdminMemberDTO memberDTO = new AdminMemberDTO();
        memberDTO.setMember_code(memberCode);
        memberDTO.setPoint(point);

        adminMemberServiceimpl.pointAdd(memberDTO);

        return "redirect:/member/pointPage?member_code=" + memberCode;
    }

    @GetMapping(value = "/pointPage")
    private String pointPage(@RequestParam int member_code,
                             Model model) {
        AdminAllMemberDTO memberDetail = adminMemberServiceimpl.memberDetail(member_code);
        model.addAttribute("memberDetail", memberDetail);

        return "admin/content/member/pointPage";
    }

    @PostMapping(value = "/memberWithdraw")
    private String memberWithdraw(@RequestParam("resultCheckbox") List<String> memberId,
                                  RedirectAttributes rttr) throws UnregistException, MessagingException, UnsupportedEncodingException {
        for (int i = 0; i < memberId.size(); i++) {
            int memberCode = Integer.parseInt(memberId.get(i));
            AdminAllMemberDTO adminAllMemberDTO = new AdminAllMemberDTO();
            adminAllMemberDTO.setMember_code(memberCode);

            adminMemberServiceimpl.memberWithdraw(adminAllMemberDTO);
        }

        rttr.addFlashAttribute("message", "회원탈퇴가 완료되었습니다.");
        return "redirect:/member/membersearchPage";
    }

    @GetMapping("/membersearch")
    public ModelAndView membersearch(@ModelAttribute AdminMemberForm form) {
        List<AdminAllMemberDTO> memberList = adminMemberServiceimpl.selectMember(form);

        int totalResult = memberList.size();

        ModelAndView modelAndView = new ModelAndView("admin/content/member/membersearch");
        modelAndView.addObject("memberList", memberList);
        modelAndView.addObject("totalResult", totalResult);

        return modelAndView;
    }

    @GetMapping("/membersearchPage")
    public String memberList() {
        return "admin/content/member/membersearch";
    }

    @GetMapping("/unregister")
    public String unregisterList() {
        return "admin/content/member/unregister";
    }
}

