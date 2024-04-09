package com.hedgehog.admin.adminManagement.controller;

import com.hedgehog.admin.adminManagement.model.dto.AdminDTO;
import com.hedgehog.admin.adminManagement.model.dto.AdminRegistrationForm;
import com.hedgehog.admin.adminManagement.model.dto.ChangePwdForm;
import com.hedgehog.admin.adminManagement.model.service.AdminManagementService;
import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.common.paging.adminManagementPaging.AdminManagementPagenation;
import com.hedgehog.common.paging.adminManagementPaging.AdminManagementSelectCriteria;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/adminManagement")
@AllArgsConstructor
public class AdminManagementController {
    private final AdminManagementService adminManagementService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/adminManagement")
    public String adminManagement(Model model,
                                  @RequestParam(value = "currentPage", defaultValue = "1") int pageNo) {
        int totalCount = adminManagementService.selectTotalCountAdminInfo();

        int limit = 5;
        int buttonAmount = 5;
        AdminManagementSelectCriteria adminManagementSelectCriteria = AdminManagementPagenation.getAdminManagementSelectCriteria(pageNo,totalCount, limit,buttonAmount);

        List<AdminDTO> adminList = adminManagementService.getAdminList(adminManagementSelectCriteria);
        model.addAttribute("adminList", adminList);
        model.addAttribute("adminManagementSelectCriteria",adminManagementSelectCriteria); // 페이징 처리를 위한 값
        return "admin/content/adminManagement/adminManagement";
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteAdmin(@RequestParam int userCode) {
        boolean success = adminManagementService.deleteAdmin(userCode);
        if (success) {
            return new ResponseEntity<>("삭제가 완료되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("삭제를 실패했습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/registAdmin")
    public String registAdmin(@ModelAttribute AdminRegistrationForm registrationForm, Model model) {
        AdminRegistrationForm newForm = new AdminRegistrationForm(registrationForm.getAdminAddId(),
                passwordEncoder.encode(registrationForm.getAdminAddPass()),
                registrationForm.getAdminAddName());
        boolean isSuccess = adminManagementService.registAdmin(newForm);
        if (isSuccess) {
            return "redirect:/adminManagement/adminManagement";
        } else {
            model.addAttribute("errorMessage", "등록에 실패했습니다. 유효한 데이터를 입력해주세요.");
            return "forward:/adminManagement/adminManagement";
        }
    }

    @PostMapping("/changePassword")
    public String adminManagement(@ModelAttribute ChangePwdForm pwdForm, Model model) {
        ChangePwdForm newPwdForm = new ChangePwdForm(pwdForm.getUserCode(), passwordEncoder.encode(pwdForm.getAdminUpdatePass()));

        boolean success = false;
        success = adminManagementService.updatePwd(newPwdForm);
        if (success) {
            return "redirect:/adminManagement/adminManagement";
        } else {
            model.addAttribute("errorMessage", "비밀번호 변경에 실패했습니다.");
            return "forward:/adminManagement/adminManagement";
        }
    }
}
