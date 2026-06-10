package com.sweetshop.sweetshop_backend.repository;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sweetshop.sweetshop_backend.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {  // String for MongoDB ObjectId
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}