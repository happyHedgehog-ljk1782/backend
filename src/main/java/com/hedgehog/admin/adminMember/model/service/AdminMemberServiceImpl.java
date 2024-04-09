package com.hedgehog.admin.adminMember.model.service;

import com.hedgehog.admin.adminMember.model.dao.AdminMemberMapper;
import com.hedgehog.admin.adminMember.model.dto.*;
import com.hedgehog.admin.exception.UnregistException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMemberServiceImpl implements AdminMemberService {
    private final AdminMemberMapper mapper;
    private final JavaMailSender javaMailSender;
    private static final String FROM_ADDRESS = "oneinfurniture0@gmail.com";

    public AdminMemberServiceImpl(AdminMemberMapper mapper, JavaMailSender javaMailSender) {
        this.mapper = mapper;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public List<AdminAllMemberDTO> selectMember(AdminMemberForm form) {
        List<AdminAllMemberDTO> memberList = mapper.selectMember(form);

        return memberList;
    }

    @Override
    @Transactional
    public void memberWithdraw(AdminAllMemberDTO adminAllMemberDTO) throws UnregistException {
        int result = mapper.updateMemberWithdrawState(adminAllMemberDTO);

        List<AdminAllMemberDTO> memberDTO = mapper.searchMember(adminAllMemberDTO);
        int result1 = mapper.insertWithdrawTable(adminAllMemberDTO);
        int result2 = mapper.updateWithdrawState(adminAllMemberDTO);

        if (!(result > 0)) {
            throw new UnregistException("상태 변경에 실패하셨습니다.");
        }
    }

    @Override
    public AdminAllMemberDTO memberDetail(int memberCode) {
        AdminAllMemberDTO memberDTO = null;

        memberDTO = mapper.memberDetail(memberCode);

        return memberDTO;
    }

    @Override
    @Transactional
    public void pointPage(AdminAllMemberDTO adminAllMemberDTO) throws UnregistException {
        int result = mapper.point(adminAllMemberDTO);

        if (!(result > 0)) {
            throw new UnregistException("상태 변경에 실패하셨습니다.");
        }
    }

    @Override
    @Transactional
    public void pointAdd(AdminMemberDTO memberDTO) throws UnregistException {
        int result = mapper.pointAdd(memberDTO);

        if (!(result > 0)) {
            throw new UnregistException("상태 변경에 실패하셨습니다.");
        }
    }

    @Override
    public AdminSendMailDTO selectMemberSendMailPage(int i) {
        AdminSendMailDTO sendMailDTO = mapper.serachMail(i);
        return sendMailDTO;
    }

    @Override
    public void sendMail(AdminSendMailDTO mailDTO) {
        try {
            for (int i = 0; i < mailDTO.getMemberId().size(); i++) {
                int memberId = Integer.parseInt(mailDTO.getMemberId().get(i));
                AdminSendMailDTO sendMailDTO = mapper.sendMail(memberId);

                mailDTO.setMailList(sendMailDTO.getMailList());

                MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();

                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF-8");

                mimeMessageHelper.setSubject(MimeUtility.encodeText(mailDTO.getTitle(), "UTF-8", "B")); //메일 제목 지정
                mimeMessageHelper.setText(mailDTO.getContent(), true); //메일 내용 지정
                mimeMessageHelper.setFrom(FROM_ADDRESS); //보내는 메일 주소 지정
                mimeMessageHelper.setTo(mailDTO.getMailList()); //받는 메일 주소 지정

                mimeMessageHelper.addInline("image", new ClassPathResource("static/admin/images/logo.png"));

                javaMailSender.send(mimeMailMessage);
            }
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("noyeonji43@naver.com");
        message.setFrom(FROM_ADDRESS);
        message.setSubject(mailDTO.getTitle());
        message.setText(mailDTO.getContent());
    }
}
