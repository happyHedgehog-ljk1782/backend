package com.hedgehog.admin.adminMember.model.service;

import com.hedgehog.admin.adminMember.model.dao.AdminUnregisterMapper;
import com.hedgehog.admin.adminMember.model.dto.AdminCustomerDTO;
import com.hedgehog.admin.adminMember.model.dto.AdminSendMailDTO;
import com.hedgehog.admin.adminMember.model.dto.AdminUnregisterDTO;
import com.hedgehog.admin.adminMember.model.dto.AdminUnregisterForm;
import com.hedgehog.admin.exception.UnregistException;
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
import java.time.LocalDate;
import java.util.List;

@Service
public class AdminUnregisterServiceImpl implements AdminUnregisterService {
    private final AdminUnregisterMapper mapper;
    private final JavaMailSender javaMailSender;
    private static final String FROM_ADDRESS = "oneinfurniture0@gmail.com";

    public AdminUnregisterServiceImpl(AdminUnregisterMapper mapper, JavaMailSender javaMailSender) {
        this.mapper = mapper;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public List<AdminUnregisterDTO> selectUnregister(AdminUnregisterForm form) {
        List<AdminUnregisterDTO> unregisterList = mapper.selectUnregister(form);
        return unregisterList;
    }

    @Override
    @Transactional
    public void causeUpdate(AdminUnregisterDTO adminUnregisterDTO) throws UnregistException, MessagingException, UnsupportedEncodingException {
        int result = 0;
        if (adminUnregisterDTO.getState().equals("adminWithdrawal")) {
            result = mapper.causeUpdate(adminUnregisterDTO);
            AdminCustomerDTO customerDTO = mapper.searchMail(adminUnregisterDTO.getUser_code());
            AdminSendMailDTO sendMailDTO = mapper.searchMailForm(2);

            LocalDate unregisterDate = LocalDate.now();
            String id = customerDTO.getId();

            String emailContent = sendMailDTO.getContent()
                    .replace("{unregisterDate}", unregisterDate.toString())
                    .replace("{memberId}", id);

            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF-8");

            mimeMessageHelper.setSubject(MimeUtility.encodeText(sendMailDTO.getTitle(), "UTF-8", "B")); //메일 제목 지정
            mimeMessageHelper.setText(emailContent, true); //메일 내용 지정
            mimeMessageHelper.setFrom(FROM_ADDRESS); //보내는 메일 주소 지정
            mimeMessageHelper.setTo(customerDTO.getEmail()); //받는 메일 주소 지정

            mimeMessageHelper.addInline("image", new ClassPathResource("static/admin/images/logo.png"));

            javaMailSender.send(mimeMailMessage);

            result++;
        } else {
            mapper.withdrawalCancel(adminUnregisterDTO);
            mapper.userTableStateUpdate(adminUnregisterDTO);
            result++;
        }
        if (!(result > 0)) {
            throw new UnregistException(" 수정에 실패하셨습니다.");
        }
    }

    @Override
    public AdminUnregisterDTO unregisterDetail(int userCode) {
        AdminUnregisterDTO adminUnregisterDTO = null;

        adminUnregisterDTO = mapper.unregisterDetail(userCode);

        return adminUnregisterDTO;
    }
}
