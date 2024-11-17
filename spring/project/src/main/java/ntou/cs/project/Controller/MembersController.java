package ntou.cs.project.Controller;

import ntou.cs.project.Service.*;
import ntou.cs.project.Deal.*;
import ntou.cs.project.Util.*;

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

    @PostMapping(value = "/regist") // 註冊帳號
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) {

        try {
            memberService.createUser(request);
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "註冊成功"));
        } catch (Exception ex) {
            System.out.println(ex);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", false,
                            "message", "註冊失敗"));
        }

    }

    @PostMapping(value = "/login") // 登入
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {

        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword());
            authenticationManager.authenticate(authentication);
            String loginToken = jwtUtil.generateToken(new HashMap<>(), request.getEmail(), null);
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "token", loginToken,
                    "message", "登入成功"));
        } catch (AuthenticationException ex) {
            System.out.println(ex);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", false,
                            "message", "登入失敗"));

        }
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
}