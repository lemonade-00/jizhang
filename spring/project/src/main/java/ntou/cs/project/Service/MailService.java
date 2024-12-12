package ntou.cs.project.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;

import ntou.cs.project.Common.*;
import ntou.cs.project.repository.*;
import ntou.cs.project.Deal.*;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SecureRandom random;

    @Autowired
    private UserRepository userRepository;

    private ConcurrentHashMap<String, VerificationCode> codeStorage = new ConcurrentHashMap<>();

    @Async
    public void sendVerificationCode(String to, String code) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(to);
        helper.setSubject("一次性驗證碼");
        helper.setText("<p>您的驗證碼為：<b>" + code + "</b></p>", true);

        mailSender.send(mimeMessage);
    }

    public String generateCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public void saveCode(String email, String code, User user) {
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(10);
        codeStorage.put(email, new VerificationCode(code, expireTime, user));
    }

    public Map<String, Object> verifyCode(VerifyRequest request) {
        VerificationCode storedCode = codeStorage.get(request.getEmail());

        if (request.getCode() == null) {
            return Map.of(
                    "status", false,
                    "message", "請輸入驗證碼");
        }

        if (storedCode == null || !storedCode.getCode().equals(request.getCode())) {
            return Map.of(
                    "status", false,
                    "message", "驗證碼錯誤");
        } else {
            if (LocalDateTime.now().isAfter(storedCode.getExpireTime())) {
                codeStorage.remove(request.getEmail());
                return Map.of(
                        "status", false,
                        "message", "驗證碼已過期");
            }
            repo(request);
            codeStorage.remove(request.getEmail());
            return Map.of(
                    "status", true);
        }
    }

    private void repo(VerifyRequest request) {
        VerificationCode storedCode = codeStorage.get(request.getEmail());
        if (storedCode.getUser() != null) {
            userRepository.insert(storedCode.getUser());
        }
    }

    @Data
    private static class VerificationCode {
        private final User user;
        private final String code;
        private final LocalDateTime expireTime;

        public VerificationCode(String code, LocalDateTime expireTime, User user) {
            this.code = code;
            this.expireTime = expireTime;
            this.user = user;
        }
    }
}