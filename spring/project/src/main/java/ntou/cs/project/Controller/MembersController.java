package ntou.cs.project.Controller;

import ntou.cs.project.Service.*;
import ntou.cs.project.Deal.*;
import ntou.cs.project.Util.*;
import ntou.cs.project.Common.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.HttpStatus;
import io.jsonwebtoken.ExpiredJwtException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/auth")
public class MembersController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MailService mailService;

    @PostMapping(value = "/regist") // 註冊帳號(發送驗證碼)
    public ResponseEntity<Object> sendCode(@RequestBody RegisterRequest request) {

        try {
            String code = mailService.generateCode();
            memberService.checkExist(request);
            User newUser = memberService.createUser(request);
            mailService.saveCode(request.getEmail(), code, newUser);
            mailService.sendVerificationCode(request.getEmail(), code);
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "驗證碼已發送至email"));
        } catch (Exception ex) {
            System.out.println(ex);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", false,
                            "message", ex.getMessage()));
        }

    }

    @PostMapping(value = "/verify") // 註冊帳號(加入資料庫)
    public ResponseEntity<Object> register(@RequestBody VerifyRequest request) {

        try {
            Map<String, Object> check = mailService.verifyCode(request);
            if (!(boolean) check.get("status")) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "status", false,
                                "message", check.get("message")));
            }
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "註冊成功"));
        } catch (Exception ex) {
            System.out.println(ex);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", false,
                            "message", ex.getMessage()));
        }

    }

    @PostMapping(value = "/forgot") // 忘記密碼(發送驗證碼)
    public ResponseEntity<Object> sendCodeForgot(@RequestBody RegisterRequest request) {

        try {
            String code = mailService.generateCode();
            memberService.checkNotExist(request);
            mailService.saveCode(request.getEmail(), code, null);
            mailService.sendVerificationCode(request.getEmail(), code);
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "驗證碼已發送至email"));
        } catch (Exception ex) {
            System.out.println(ex);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", false,
                            "message", ex.getMessage()));
        }

    }

    @PostMapping(value = "/forgotcheck") // 驗證碼確認
    public ResponseEntity<Object> checkCode(@RequestBody VerifyRequest request) {

        try {
            Map<String, Object> check = mailService.verifyCode(request);
            if (!(boolean) check.get("status")) {
                return ResponseEntity.status(403)
                        .body(Map.of(
                                "status", false,
                                "message", check.get("message")));
            }
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "註冊成功"));
        } catch (Exception ex) {
            System.out.println(ex);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", false,
                            "message", ex.getMessage()));
        }

    }

    @PostMapping(value = "/resetpass") // 重設密碼
    public ResponseEntity<Object> resetPassword(@RequestBody RegisterRequest request) {

        try {
            memberService.resetPass(request);
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "驗證碼已發送至email"));
        } catch (Exception ex) {
            System.out.println(ex);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", false,
                            "message", ex.getMessage()));
        }

    }

    @PostMapping(value = "/login") // 登入
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userId = ((CustomUserDetails) userDetails).getUserID();
            Map<String, String> claims = new HashMap<>();
            claims.put("userId", userId);
            String loginToken = jwtUtil.generateToken(claims, request.getEmail(), null);
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "token", loginToken,
                    "message", "登入成功"));
        } catch (AuthenticationException ex) {
            System.out.println(ex);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", false,
                            "message", ex.getMessage()));
        }
    }

    @GetMapping // 取得帳號資訊
    public ResponseEntity<Object> getMember(@ModelAttribute QueryParameter param) {
        String userId = getUserID();

        User user = memberService.getMember(userId);
        Map<String, String> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("password", user.getPassword());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update")
    public ResponseEntity<Object> update(
            @RequestBody RegisterRequest payload,
            HttpServletRequest request) {

        try {
            String authToken = request.getHeader("Authorization");

            jwtUtil.validateToken(authToken);

            String member = jwtUtil.getSubjectFromToken(authToken);
            memberService.updateUser(member, payload);

            return ResponseEntity.ok()
                    .body(Map.of(
                            "status", true,
                            "message", "修改成功"));
        } catch (ExpiredJwtException ex) {
            // JWT逾期拋出例外
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "status", false,
                            "message", "請登入"));
        } catch (Exception ex) {
            System.out.println(ex);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", false,
                            "message", "修改失敗"));
        }

    }

    private String getUserID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();

        String userId = userDetails.getID();
        return userId;
    }
}