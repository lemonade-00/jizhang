package ntou.cs.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import ntou.cs.project.Common.*;

public interface BudgetRepository extends MongoRepository<Budget, String> {

    @Query("{ 'userID': ?0 }")
    public Budget findByUserId(String userID);
}
