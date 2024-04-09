package com.hedgehog.admin.adminService.model.service;

import com.hedgehog.admin.adminMember.model.dto.AdminCustomerDTO;
import com.hedgehog.admin.adminMember.model.dto.AdminSendMailDTO;
import com.hedgehog.admin.adminService.model.dao.AdminInquiryMapper;
import com.hedgehog.admin.adminService.model.dto.AdminInquiryDTO;
import com.hedgehog.admin.adminService.model.dto.AdminInquiryForm;
import com.hedgehog.admin.exception.BoardException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminInquiryServiceImpl implements AdminInquiryService {

    private final AdminInquiryMapper mapper;
    private final JavaMailSender javaMailSender;

    private static final String FROM_ADDRESS = "oneinfurniture0@gmail.com";

    public AdminInquiryServiceImpl(AdminInquiryMapper mapper, JavaMailSender javaMailSender) {
        this.mapper = mapper;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public List<AdminInquiryDTO> searchInquiry(AdminInquiryForm form) {
        List<AdminInquiryDTO> inquiryList = mapper.searchInquiry(form);
        return inquiryList;
    }

    @Override
    @Transactional
    public void inqStateUpdate(AdminInquiryDTO inquiryDTO) throws BoardException {
        int result = mapper.inqStateUpdate(inquiryDTO);

        if (!(result > 0)) {
            throw new BoardException("상태 변경에 실패하셨습니다.");
        }
    }

    @Override
    public AdminInquiryDTO inquiryDetail(int inquiryCode) {
        AdminInquiryDTO adminInquiryDTO = null;

        adminInquiryDTO = mapper.inquiryDetail(inquiryCode);

        return adminInquiryDTO;
    }
}
