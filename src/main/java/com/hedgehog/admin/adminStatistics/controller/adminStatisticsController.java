package com.hedgehog.admin.adminStatistics.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/statistics")
public class adminStatisticsController {
    @GetMapping("/statistics")
    public String statistics() {
        return "admin/content/statistics/statistics";
    }
}
