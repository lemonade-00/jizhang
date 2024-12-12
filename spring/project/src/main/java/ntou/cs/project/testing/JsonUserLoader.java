package ntou.cs.project.testing;

import ntou.cs.project.Common.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JsonUserLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourceLoader resourceLoader;

    public JsonUserLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<User> loadUsers() {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/users.json");
            if (!resource.exists()) {
                throw new RuntimeException("users.json not found");
            }
            System.out.println("Loading users from JSON...");
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load users from JSON", e);
        }
    }

    public List<Account> loadAccount() {
        this.objectMapper.registerModule(new JavaTimeModule()); // 註冊 JavaTimeModule
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 確保序列化為 ISO 日期格式
        try {
            Resource resource = resourceLoader.getResource("classpath:static/test.json");
            if (!resource.exists()) {
                throw new RuntimeException("test.json not found");
            }
            System.out.println("Loading accounts from JSON...");
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load accounts from JSON", e);
        }
    }

}
