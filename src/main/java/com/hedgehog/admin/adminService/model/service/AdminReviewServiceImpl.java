package com.hedgehog.admin.adminService.model.service;

import com.hedgehog.admin.adminService.model.dao.AdminReviewMapper;
import com.hedgehog.admin.adminService.model.dto.AdminReviewDTO;
import com.hedgehog.admin.adminService.model.dto.AdminReviewForm;
import com.hedgehog.admin.exception.BoardException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminReviewServiceImpl implements AdminReviewService {
    private final AdminReviewMapper mapper;

    public AdminReviewServiceImpl(AdminReviewMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminReviewDTO> searchReview(AdminReviewForm form) {
        List<AdminReviewDTO> reviewList = mapper.searchReview(form);
        return reviewList;
    }

    @Override
    @Transactional
    public void revStateUpdate(AdminReviewDTO reviewDTO) throws BoardException {
        int result = mapper.revStateUpdate(reviewDTO);

        if (!(result > 0)) {
            throw new BoardException("상태 변경에 실패하셨습니다.");
        }
    }

    @Override
    public AdminReviewDTO reviewDetail(int Review_code) {
        AdminReviewDTO adminReviewDTO = null;
        adminReviewDTO = mapper.reviewDetail(Review_code);

        return adminReviewDTO;
    }
}
