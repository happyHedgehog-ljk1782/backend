package com.hedgehog.client.auth.model.service;

import com.hedgehog.client.auth.model.dao.AuthMapper;
import com.hedgehog.client.auth.model.dto.*;
import com.hedgehog.common.common.exception.UserCertifiedException;
import com.hedgehog.common.common.exception.UserRegistPostException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthMapper mapper;
    private final JavaMailSender javaMailSender;
    private final String FROM_ADDRESS = "oneinfurniture0@gmail.com";

    @Override
    public boolean selectUserById(String userId) {
        String result = mapper.selectUserById(userId);
        return result != null ? true : false; 
    }

    @Override
    public boolean selectMemberByEmail(String email) {
        String result = mapper.selectMemberByEmail(email);
        return result != null ? true : false;
    }

    @Override
    @Transactional
    public int selectCertifiedNumber(String randomCode) throws UserCertifiedException {
        int result = mapper.insertCode(randomCode);
        if (result <= 0)
            throw new UserCertifiedException("입력에 실패했습니다.");
        int certifiedNumber = mapper.selectLastInsertCertifiedNumber();

        return certifiedNumber;
    }

    @Override
    public boolean certifyEmail(int inputCertifiedCode, String certifiedKey) {
        int successCount = mapper.certifyEmail(inputCertifiedCode, certifiedKey);
        System.out.println(inputCertifiedCode);
        System.out.println(certifiedKey);
        return successCount == 0;
    }

    @Override
    @Transactional
    public boolean registMember(MemberDTO newMember) throws MessagingException, UnsupportedEncodingException {
        boolean resultIsExistId = mapper.selectUserById(newMember.getUserId()) != null ? true : false;
        if (resultIsExistId) {
            return false; 
        }
        boolean resultNotCreateId = mapper.insertUser(newMember) != 1 ? true : false;
        if (resultNotCreateId) {
            return false; 
        }
        Integer userCode = mapper.selectUserCode();
        if (userCode == null) {
            return false; 
        }
        boolean resultNotCreateAuthorityList = mapper.insertAuthorityList(userCode) != 1 ? true : false;
        if (resultNotCreateAuthorityList) {
            return false; 
        }
        boolean resultIsExistEmail = mapper.selectMemberByEmail(newMember.getEmail()) != null ? true : false;
        if (resultIsExistEmail) {
            return false;
        }
        boolean resultIsNotCustomer = mapper.insertCustomer(userCode, newMember) != 1 ? true : false;
        if (resultIsNotCustomer) {
            return false; 
        }
        boolean resultIsNotMember = mapper.insertMember(userCode, newMember) != 1 ? true : false;
        if (resultIsNotMember) {
            return false; 
        }

        boolean resultGreetingMail = sendRegistEmail(newMember.getEmail(), newMember.getUserId());

        return resultGreetingMail; 
    }

    private boolean sendRegistEmail(String email, String userId) throws MessagingException, UnsupportedEncodingException {
        int result = 0;
        final int greetingMailFormCode = 1;
        MailDTO mailDTO = mapper.searchMailForm(greetingMailFormCode); 

        String emailContent = mailDTO.getContent()
                .replace("{memberId}", userId);

        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF-8");

        mimeMessageHelper.setSubject(MimeUtility.encodeText(mailDTO.getTitle(), "UTF-8", "B")); //메일 제목 지정
        mimeMessageHelper.setText(emailContent, true); //메일 내용 지정
        mimeMessageHelper.setFrom(FROM_ADDRESS); //보내는 메일 주소 지정
        mimeMessageHelper.setTo(email); //받는 메일 주소 지정

        mimeMessageHelper.addInline("image", new ClassPathResource("static/admin/images/logo.png"));

        javaMailSender.send(mimeMailMessage);

        result++;
        if (!(result > 0)) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public UserDetails findByUserId(String username) {
        LoginUserDTO user = mapper.findByUsername(username);
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("아이디를 잘못입력했습니다.");
        }
        int userCode = user.getUserCode();
        boolean isUpdateConnectionDate = mapper.updateConnectionDate(userCode) == 1;
        if (!isUpdateConnectionDate) {
            throw new InternalAuthenticationServiceException("현재 아이디의 connection_date의 값을 넣지 못했습니다.");
        }
        UserDetails login = new LoginDetails(user);

        return login;
    }

    @Override
    public List<PostDTO> getRegistPosts() throws UserRegistPostException {
        List<PostDTO> postList = mapper.getRegistPosts();
        if (postList.size() != 2) {
            throw new UserRegistPostException("개인정보처리방침 또는 이용약관을 가져오지 못했습니다.");
        }
        return postList;
    }


    @Override
    public boolean sendCheckEmailMail(String email, String randomCode) throws MessagingException, UnsupportedEncodingException {
        int result = 0;
        MailDTO mailDTO = mapper.searchMailForm(9); // 인증번호 발송 메일

        String emailContent = mailDTO.getContent()
                .replace("{randomCode}", randomCode);

        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF-8");

        mimeMessageHelper.setSubject(MimeUtility.encodeText(mailDTO.getTitle(), "UTF-8", "B")); //메일 제목 지정
        mimeMessageHelper.setText(emailContent, true); //메일 내용 지정
        mimeMessageHelper.setFrom(FROM_ADDRESS); //보내는 메일 주소 지정
        mimeMessageHelper.setTo(email); //받는 메일 주소 지정

        mimeMessageHelper.addInline("image", new ClassPathResource("static/admin/images/logo.png"));

        javaMailSender.send(mimeMailMessage);

        result++;
        if (!(result > 0)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean sendPasswordMail(String email, String password) throws MessagingException, UnsupportedEncodingException {
        int result = 0;
        MailDTO mailDTO = mapper.searchMailForm(3); // 인증번호 발송 메일

        String emailContent = mailDTO.getContent()
                .replace("{password}", password);

        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true, "UTF-8");

        mimeMessageHelper.setSubject(MimeUtility.encodeText(mailDTO.getTitle(), "UTF-8", "B")); //메일 제목 지정
        mimeMessageHelper.setText(emailContent, true); //메일 내용 지정
        mimeMessageHelper.setFrom(FROM_ADDRESS); //보내는 메일 주소 지정
        mimeMessageHelper.setTo(email); //받는 메일 주소 지정

        mimeMessageHelper.addInline("image", new ClassPathResource("static/admin/images/logo.png"));

        javaMailSender.send(mimeMailMessage);

        result++;
        if (!(result > 0)) {
            return false;
        }
        return true;
    }
}
