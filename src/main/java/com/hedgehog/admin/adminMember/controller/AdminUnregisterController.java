package com.hedgehog.admin.adminMember.controller;

import com.hedgehog.admin.adminMember.model.dto.AdminUnregisterDTO;
import com.hedgehog.admin.adminMember.model.dto.AdminUnregisterForm;
import com.hedgehog.admin.adminMember.model.service.AdminUnregisterServiceImpl;
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
import java.util.List;

@Controller
@RequestMapping("/unregister")
public class AdminUnregisterController {
    private final AdminUnregisterServiceImpl adminUnregisterServiceimpl;

    public AdminUnregisterController(AdminUnregisterServiceImpl adminUnregisterServiceimpl) {
        this.adminUnregisterServiceimpl = adminUnregisterServiceimpl;
    }

    @GetMapping(value = "unregisterDetail")
    private String unregisterDetail(@RequestParam("userCode") int userCode,
                                    Model model) {
        AdminUnregisterDTO adminUnregisterDTO = adminUnregisterServiceimpl.unregisterDetail(userCode);

        model.addAttribute("unregisterDetail", adminUnregisterDTO);

        return "admin/content/member/unregisterDetail";
    }

    @PostMapping(value = "/stateUpdate")
    private String orderStateUpdate(@RequestParam("resultCheckbox") List<String> selectedId,
                                    @RequestParam("selectCommit") String selectedState,
                                    RedirectAttributes rttr) throws OrderStateUpdateException, UnregistException, MessagingException, UnsupportedEncodingException {
        for (int i = 0; i < selectedId.size(); i++) {
            int userCode = Integer.parseInt(selectedId.get(i));
            AdminUnregisterDTO adminUnregisterDTO = new AdminUnregisterDTO();
            adminUnregisterDTO.setUser_code(userCode);
            adminUnregisterDTO.setState(selectedState);

            adminUnregisterServiceimpl.causeUpdate(adminUnregisterDTO);
        }

        rttr.addFlashAttribute("message", "상태가 변경되었습니다.");
        return "redirect:/unregister/unregisterSearch";
    }

    @GetMapping("/unregisterSearch")
    public ModelAndView unregister(@ModelAttribute AdminUnregisterForm form) {
        List<AdminUnregisterDTO> unregisterList = adminUnregisterServiceimpl.selectUnregister(form);
        int totalResult = unregisterList.size();
        int countY = 0;
        int countN = 0;
        int countI = 0;
        for (int i = 0; i < unregisterList.size(); i++) {
            String state = unregisterList.get(i).getState();
            if (unregisterList.get(i).getCause().equals("강제 탈퇴")) {
                countN++;
            } else if (unregisterList.get(i).getCause().equals("탈퇴 취소")) {
                countY++;
            } else {
                countI++;
            }
        }
        ModelAndView modelAndView = new ModelAndView("admin/content/member/unregister");
        modelAndView.addObject("unregisterList", unregisterList);
        modelAndView.addObject("totalResult", totalResult);
        modelAndView.addObject("countY", countY);
        modelAndView.addObject("countN", countN);
        modelAndView.addObject("countI", countI);

        return modelAndView;
    }
}
