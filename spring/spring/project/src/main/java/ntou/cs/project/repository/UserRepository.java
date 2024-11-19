package ntou.cs.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import ntou.cs.project.Common.*;

public interface UserRepository extends MongoRepository<User, String> {
    @Query("{ 'email' : ?0 }")
    public User findByEmail(String email);

    @Query("{ 'ID': ?0 }")
    User getUserByID(String id);
}