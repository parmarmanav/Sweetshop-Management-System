package com.sweetshop.sweetshop_backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.sweetshop.sweetshop_backend.model.Sweet;

import java.util.List;
import java.util.Optional;

@Repository
public interface SweetRepository extends MongoRepository<Sweet, String> {
    
    // Basic search methods
    List<Sweet> findByCategory(String category);
    Optional<Sweet> findByName(String name);
    List<Sweet> findByPriceBetween(double minPrice, double maxPrice);
    List<Sweet> findByQuantityGreaterThan(int quantity);
    
    // Case-insensitive search methods for better user experience
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Sweet> findByNameContainingIgnoreCase(String name);
    
    @Query("{'category': {$regex: ?0, $options: 'i'}}")
    List<Sweet> findByCategoryContainingIgnoreCase(String category);
    
    // Complex multi-criteria search query
    @Query("{ $and: [ " +
           "{ $or: [ {'name': {$regex: ?0, $options: 'i'}}, {?0: null} ] }, " +
           "{ $or: [ {'category': {$regex: ?1, $options: 'i'}}, {?1: null} ] }, " +
           "{ $or: [ {'price': {$gte: ?2}}, {?2: null} ] }, " +
           "{ $or: [ {'price': {$lte: ?3}}, {?3: null} ] } " +
           "] }")
    List<Sweet> findByMultipleCriteria(String name, String category, Double minPrice, Double maxPrice);
    
    // Additional useful methods
    boolean existsByName(String name);
    List<Sweet> findByPriceGreaterThan(double price);
    List<Sweet> findByPriceLessThan(double price);
    
    // Find available sweets (quantity > 0)
    @Query("{'quantity': {$gt: 0}}")
    List<Sweet> findAvailableSweets();
}