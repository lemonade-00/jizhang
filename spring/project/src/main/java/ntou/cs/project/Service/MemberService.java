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

    public User createUser(RegisterRequest request) throws Exception {
        User newUser = new User();

        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        return newUser;
    }

    public void checkExist(RegisterRequest request) throws Exception {
        User dbUser = userRepository.findByEmail(request.getEmail());
        if (dbUser != null) {
            throw new Exception("該郵件已被註冊");
        }

    }

    public void checkNotExist(RegisterRequest request) throws Exception {
        User dbUser = userRepository.findByEmail(request.getEmail());
        if (dbUser == null) {
            throw new Exception("該郵件尚未註冊");
        }
    }

    public User getMember(String member) { // 找出特定使用者
        return userRepository.getUserByID(member);
    }

    public void updateUser(String id, RegisterRequest user) throws Exception {

        User existingUser = getMember(id);
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

    public void resetPass(RegisterRequest user) throws Exception {
        User existingUser = userRepository.findByEmail(user.getEmail());
        String newPassword = passwordEncoder.encode(user.getPassword());
        existingUser.setPassword(newPassword);
        userRepository.save(existingUser);
    }

}
