package ntou.cs.project.Service;

import ntou.cs.project.Common.*;
import ntou.cs.project.Deal.*;
import ntou.cs.project.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class MemberService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public void createUser(RegisterRequest request) throws Exception {
        User newUser = new User();

        User dbUser = userRepository.findByEmail(request.getEmail());
        // 資料庫搜尋此email是否已註冊過
        if (dbUser != null) {
            throw new Exception("該郵件已被註冊");
        }

        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        System.out.println(newUser.getEmail());
        System.out.println(newUser.getPassword());
        // 將新用戶加入資料庫
        userRepository.insert(newUser);
    }

    public User getMember(String member) { // 找出特定使用者
        User user = new User();
        return user;
        // 根據email回傳該名使用者 並將這兩行刪掉
    }

    public void updateUser(String member, RegisterRequest user) throws Exception {

        // 資料庫中找出該使用者
        User existingUser = getMember(member);
        boolean updated = false;
        if (user.getPassword() != null) {
            String newPassword = passwordEncoder.encode(user.getPassword());
            existingUser.setPassword(newPassword);
            updated = true;
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
            updated = true;
        }
        if (updated) {
            userRepository.save(existingUser);
        }
    }

}
