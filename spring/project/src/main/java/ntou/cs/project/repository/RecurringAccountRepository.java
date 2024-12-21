package ntou.cs.project.repository;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import ntou.cs.project.Common.*;

public interface RecurringAccountRepository extends MongoRepository<RecurringAccount, String> {

    @Query("{ 'userID': ?0 }")
    ArrayList<RecurringAccount> findByUserId(String userID);

    @Query("{ 'isActive': ?0 }")
    ArrayList<RecurringAccount> findByIsRecurring(boolean isActive);

    @Query("{ 'ID': ?0 }")
    RecurringAccount getAccountByID(String id);
}
