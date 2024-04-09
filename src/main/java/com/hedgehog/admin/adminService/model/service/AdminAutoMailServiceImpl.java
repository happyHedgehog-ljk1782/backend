package com.hedgehog.admin.adminService.model.service;

import com.hedgehog.admin.adminService.model.dao.AdminAutoMapper;
import com.hedgehog.admin.adminService.model.dto.AdminAutoMailDTO;
import com.hedgehog.admin.adminService.model.dto.AdminAutoMailForm;
import com.hedgehog.admin.exception.AdminProductAddException;
import com.hedgehog.client.board.model.dto.UploadedImageDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ListUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AdminAutoMailServiceImpl implements AdminAutoMailService{
    private final AdminAutoMapper mapper;
    private final JavaMailSender javaMailSender;
    private static final String FROM_ADDRESS = "oneinfurniture0@gmail.com";

    public AdminAutoMailServiceImpl(AdminAutoMapper mapper, JavaMailSender javaMailSender) {
        this.mapper = mapper;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public AdminAutoMailDTO previewMail(int mailCode) {
        AdminAutoMailDTO adminAutoMailDTO = mapper.previewMail(mailCode);

        return adminAutoMailDTO;
    }

    @Override
    @Transactional
    public void modifyMail(AdminAutoMailDTO mailDTO) throws AdminProductAddException {
        int result = mapper.modifyMail(mailDTO);
        if(!(result > 0)){
            throw new AdminProductAddException("메일 수정에 실패하였습니다.");
        }
    }

    @Override
    @Transactional
    public boolean sendMail(List<UploadedImageDTO> uploadedImageList, String title, String summernote, String chooseMember) throws MessagingException, UnsupportedEncodingException {
        String[] searchEmailList = mapper.searchEmailList();
        AdminAutoMailDTO adminAutoMailDTO = new AdminAutoMailDTO();
        adminAutoMailDTO.setTitle(title);
        adminAutoMailDTO.setContent(summernote);

        int result = mapper.insertMailHistory(adminAutoMailDTO);

        int mailCode = adminAutoMailDTO.getMail_code();

        int result2 = mapper.imgInsert(uploadedImageList, mailCode);

        if(result2 != 1){
            return false;
        }
        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF-8");

        mimeMessageHelper.setSubject(MimeUtility.encodeText(adminAutoMailDTO.getTitle(), "UTF-8", "B")); //메일 제목 지정
        mimeMessageHelper.setText(adminAutoMailDTO.getContent(), true); //메일 내용 지정
        mimeMessageHelper.setFrom(FROM_ADDRESS); //보내는 메일 주소 지정
        mimeMessageHelper.setTo(searchEmailList); //받는 메일 주소 지정
        mimeMessageHelper.setBcc(searchEmailList);

        javaMailSender.send(mimeMailMessage);

        return true;
    }

    @Override
    public List<AdminAutoMailDTO> searchEmailHistory(AdminAutoMailForm form) {
        List<AdminAutoMailDTO> mailList = mapper.searchEmailHistory(form);

        return mailList;
    }

    @Override
    public AdminAutoMailDTO emailDetail(int mailCode) {
        AdminAutoMailDTO mailList = mapper.emailDetail(mailCode);

        return mailList;
    }

    @Override
    public boolean sendMailOnlyString(String title, String summernote, String chooseMember) throws MessagingException, UnsupportedEncodingException {
        String[] searchEmailList = mapper.searchEmailList();

        AdminAutoMailDTO adminAutoMailDTO = new AdminAutoMailDTO();
        adminAutoMailDTO.setTitle(title);
        adminAutoMailDTO.setContent(summernote);

        int result = mapper.insertMailHistory(adminAutoMailDTO);
        int mailCode = adminAutoMailDTO.getMail_code();

        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF-8");

        mimeMessageHelper.setSubject(MimeUtility.encodeText(adminAutoMailDTO.getTitle(), "UTF-8", "B")); //메일 제목 지정
        mimeMessageHelper.setText(adminAutoMailDTO.getContent(), true); //메일 내용 지정
        mimeMessageHelper.setFrom(FROM_ADDRESS); //보내는 메일 주소 지정
        mimeMessageHelper.setTo(searchEmailList); //받는 메일 주소 지정
        mimeMessageHelper.setBcc(searchEmailList);

        javaMailSender.send(mimeMailMessage);

        return true;
    }
}



