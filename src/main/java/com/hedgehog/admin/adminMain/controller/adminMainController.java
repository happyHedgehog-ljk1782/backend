package com.hedgehog.admin.adminMain.controller;

import com.hedgehog.admin.adminMain.model.dto.AdminDailyVisitorsDTO;
import com.hedgehog.admin.adminMain.model.dto.AdminMainStatisticsDTO;
import com.hedgehog.admin.adminMain.model.service.AdminDailyVisitorsServiceImpl;
import com.hedgehog.admin.adminService.model.dto.AdminInquiryDTO;
import com.hedgehog.admin.adminService.model.dto.AdminReviewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/adminmain")
public class adminMainController {
    private final AdminDailyVisitorsServiceImpl adminDailyVisitorsServiceImpl;

    public adminMainController(AdminDailyVisitorsServiceImpl adminDailyVisitorsServiceImpl) {
        this.adminDailyVisitorsServiceImpl = adminDailyVisitorsServiceImpl;
    }

    @GetMapping("/main")
    public ModelAndView adminmain(Model model) {
        List<AdminDailyVisitorsDTO> dailyVisitorsDTO = adminDailyVisitorsServiceImpl.dailyVisitors();

        int visitorCount = dailyVisitorsDTO.size();
        AdminMainStatisticsDTO adminMainStatisticsDTO = adminDailyVisitorsServiceImpl.dailySales();

        List<AdminInquiryDTO> adminInquiryDTO = adminDailyVisitorsServiceImpl.inquiry();

        List<AdminReviewDTO> adminReviewDTOS = adminDailyVisitorsServiceImpl.review();

        ModelAndView mv = new ModelAndView();
        model.addAttribute("dailyVisitors", visitorCount);
        model.addAttribute("dailySales", adminMainStatisticsDTO);
        model.addAttribute("inquiry", adminInquiryDTO);
        model.addAttribute("review", adminReviewDTOS);
        mv.setViewName("admin/content/main/admin-main");

        return mv;
    }
}
