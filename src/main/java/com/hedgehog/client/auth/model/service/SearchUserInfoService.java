package com.hedgehog.client.auth.model.service;

import com.hedgehog.client.auth.model.dao.SearchUserInfoMapper;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Random;

@Service
@AllArgsConstructor
public class SearchUserInfoService {
    private final SearchUserInfoMapper mapper;
    private final AuthServiceImpl authService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Integer selectMemberByEmail(String email) throws MessagingException, UnsupportedEncodingException {
        Integer certificationCode = mapper.selectMemberByEmail(email);
        if (certificationCode == null) {
            return certificationCode;
        }

        int min = 100000;
        int max = 1000000;
        String randomCode = String.valueOf(new Random().nextInt(max - min) + min);
        mapper.updateCertificationNumber(certificationCode, randomCode);
        boolean result = authService.sendCheckEmailMail(email, randomCode);

        if (result == false) {
            return -1;
        }

        return certificationCode;
    }

    public boolean certifyEmail(int inputCertifiedCode, String certifiedKey) {
        boolean isPass = mapper.certifyEmail(inputCertifiedCode, certifiedKey) == 1 ? true : false;
        return isPass;
    }

    public String findUserId(String email, int emailAuthenticationNumber, int hiddenCertifiedKey) {
        String userId = mapper.findUserId(email, emailAuthenticationNumber, hiddenCertifiedKey);
        return userId;
    }

    @Transactional
    public Integer selectMemberByUserIdAndEmail(String userId, String email) throws MessagingException, UnsupportedEncodingException {
        Integer certificationCode = mapper.selectMemberByUserIdAndEmail(userId, email);

        if (certificationCode == null) {
            return certificationCode;
        }

        int min = 100000;
        int max = 1000000;
        String randomCode = String.valueOf(new Random().nextInt(max - min) + min);
        mapper.updateCertificationNumber(certificationCode, randomCode);
        boolean result = authService.sendCheckEmailMail(email, randomCode);

        if (result == false) {
            return -1;
        }
        return certificationCode;
    }

    @Transactional
    public String insertUserPassword(String userId, String email, int emailAuthenticationNumber, int hiddenCertifiedKey) throws MessagingException, UnsupportedEncodingException {
        Integer userCode = mapper.findUser(userId, email, emailAuthenticationNumber, hiddenCertifiedKey);
        String newUserPassword = null;
        if (userCode != null) {
            newUserPassword = generateRandomPassword();
            mapper.insertNewUserPassword(userCode, passwordEncoder.encode(newUserPassword));

            boolean result = authService.sendPasswordMail(email, newUserPassword);
            if (!result) {
                return "sendMiss";
            }
        }
        return newUserPassword;
    }

    public String generateRandomPassword() {
        String lowercaseChars = "abcdefghijklmnopqrstuvwxyz";
        String uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digitChars = "0123456789";
        String specialChars = "!@#$%^&*";

        String allChars = lowercaseChars + uppercaseChars + digitChars + specialChars;

        SecureRandom random = new SecureRandom();
        int passwordLength = 12;

        StringBuilder passwordBuilder = new StringBuilder();

        passwordBuilder.append(getRandomChar(lowercaseChars, random));
        passwordBuilder.append(getRandomChar(uppercaseChars, random));
        passwordBuilder.append(getRandomChar(digitChars, random));
        passwordBuilder.append(getRandomChar(specialChars, random));
        for (int i = 4; i < passwordLength; i++) {
            passwordBuilder.append(getRandomChar(allChars, random));
        }
        return passwordBuilder.toString();
    }

    private char getRandomChar(String source, SecureRandom random) {
        int randomIndex = random.nextInt(source.length());
        return source.charAt(randomIndex);
    }
}
