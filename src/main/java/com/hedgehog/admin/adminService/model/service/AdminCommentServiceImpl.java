package com.hedgehog.admin.adminService.model.service;

import com.hedgehog.admin.adminMember.model.dto.AdminCustomerDTO;
import com.hedgehog.admin.adminMember.model.dto.AdminSendMailDTO;
import com.hedgehog.admin.adminService.model.dao.AdminCommentMapper;
import com.hedgehog.admin.adminService.model.dto.AdminCommentDTO;
import com.hedgehog.admin.exception.BoardException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

@Service
public class AdminCommentServiceImpl implements AdminCommentService {
    private final AdminCommentMapper mapper;
    private final JavaMailSender javaMailSender;
    private static final String FROM_ADDRESS = "oneinfurniture0@gmail.com";


    public AdminCommentServiceImpl(AdminCommentMapper mapper, JavaMailSender javaMailSender) throws BoardException {
        this.mapper = mapper;
        this.javaMailSender = javaMailSender;
    }

    @Override
    @Transactional
    public void inquiryComment(AdminCommentDTO adminCommentDTO) throws BoardException, MessagingException, UnsupportedEncodingException {
        if ("N".equals(adminCommentDTO.getAnswer_state())) {
            int result = mapper.inquiryComment(adminCommentDTO);
            int result2 = mapper.updateState(adminCommentDTO);

            AdminCustomerDTO customerDTO = mapper.searchMail(adminCommentDTO.getUser_code());
            AdminSendMailDTO sendMailDTO = mapper.searchmailForm(8);

            String inquiryTitle = adminCommentDTO.getInqtitle();
            String inquiryContent = adminCommentDTO.getInqcontent();
            String comment = adminCommentDTO.getContent();
            String emailContent = sendMailDTO.getContent()
                    .replace("{inquiryTitle}", inquiryTitle)
                    .replace("{inquiryContent}", inquiryContent)
                    .replace("{comment}", comment);

            MimeMessage mimeMileMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMileMessage, true, "UTF-8");

            mimeMessageHelper.setSubject(MimeUtility.encodeText(sendMailDTO.getTitle(), "UTF-8", "B"));
            mimeMessageHelper.setText(emailContent, true);
            mimeMessageHelper.setFrom(FROM_ADDRESS);
            mimeMessageHelper.setTo(customerDTO.getEmail());

            mimeMessageHelper.addInline("image", new ClassPathResource("static/admin/images/logo.png"));

            javaMailSender.send(mimeMileMessage);

            result++;
            if (result <= 0) {
                throw new BoardException("댓글 등록에 실패하셨습니다.");
            }
        } else if ("Y".equals(adminCommentDTO.getAnswer_state())) {
            int result = mapper.inquiryCommentUpdate(adminCommentDTO);
            if (result <= 0) {
                throw new BoardException("댓글 업데이트에 실패하셨습니다.");
            }
        }
    }
}

