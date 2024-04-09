package com.hedgehog.admin.adminMain.model.service;

import com.hedgehog.admin.adminMain.model.dao.AdminDailyVisitorsMapper;
import com.hedgehog.admin.adminMain.model.dto.AdminDailyVisitorsDTO;
import com.hedgehog.admin.adminMain.model.dto.AdminMainStatisticsDTO;
import com.hedgehog.admin.adminService.model.dto.AdminInquiryDTO;
import com.hedgehog.admin.adminService.model.dto.AdminReviewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDailyVisitorsServiceImpl implements AdminDailyVisitorsService {
    private final AdminDailyVisitorsMapper mapper;

    public AdminDailyVisitorsServiceImpl(AdminDailyVisitorsMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminDailyVisitorsDTO> dailyVisitors() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        return mapper.dailyVisitors(startOfDay, endOfDay);
    }

    @Override
    public AdminMainStatisticsDTO dailySales() {
        AdminMainStatisticsDTO adminMainStatisticsDTO = new AdminMainStatisticsDTO();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        List<String> dailySales = mapper.dailySales(startOfDay, endOfDay);
        int result = 0;
        for (int i = 0; i < dailySales.size(); i++) {
            result += Integer.parseInt(dailySales.get((i)));
        }
        adminMainStatisticsDTO.setSales(result);

        List<String> dailySaleVolume = mapper.dailySaleVolume(startOfDay, endOfDay);
        int result1 = dailySaleVolume.size();
        adminMainStatisticsDTO.setSaleVolume(result1);

        List<String> dailyReview = mapper.dailyReviews(startOfDay, endOfDay);
        int result2 = dailyReview.size();
        adminMainStatisticsDTO.setReviews(result2);

        List<String> dailyUser = mapper.dailyUser(startOfDay, endOfDay);
        int result3 = dailyUser.size();
        adminMainStatisticsDTO.setUser(result3);

        List<String> dailyInquiry = mapper.dailyInquiry(startOfDay, endOfDay);
        int result4 = dailyInquiry.size();
        adminMainStatisticsDTO.setInquiry(result4);

        List<String> dailyOrder = mapper.dailyOrder(startOfDay, endOfDay);
        int result5 = dailyOrder.size();
        adminMainStatisticsDTO.setOrder(result5);

        List<String> dailyDelivery = mapper.dailyDelivery(startOfDay, endOfDay);
        int result6 = dailyDelivery.size();
        adminMainStatisticsDTO.setDelivery(result6);

        return adminMainStatisticsDTO;
    }

    @Override
    public List<AdminInquiryDTO> inquiry() {
        List<AdminInquiryDTO> adminInquiryDTO = mapper.searchInquiry();
        return adminInquiryDTO;
    }

    @Override
    public List<AdminReviewDTO> review() {
        List<AdminReviewDTO> reviewDTO = mapper.searchReview();
        return reviewDTO;
    }
}
