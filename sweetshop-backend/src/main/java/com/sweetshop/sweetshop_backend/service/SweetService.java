package com.sweetshop.sweetshop_backend.service;

import org.springframework.stereotype.Service;
import com.sweetshop.sweetshop_backend.model.Sweet;
import com.sweetshop.sweetshop_backend.repository.SweetRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SweetService {

    private final SweetRepository sweetRepository;

    public SweetService(SweetRepository sweetRepository) {
        this.sweetRepository = sweetRepository;
    }

    public Sweet createSweet(Sweet sweet) {
        return sweetRepository.save(sweet);
    }

    public List<Sweet> getAllSweets() {
        return sweetRepository.findAll();
    }

    public List<Sweet> searchSweetsByName(String name) {
        return sweetRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Sweet> searchSweetsByCategory(String category) {
        return sweetRepository.findByCategoryContainingIgnoreCase(category);
    }

    public List<Sweet> searchSweetsByPriceRange(double minPrice, double maxPrice) {
        return sweetRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Sweet> searchSweets(String name, String category, Double minPrice, Double maxPrice) {
        return sweetRepository.findByMultipleCriteria(name, category, minPrice, maxPrice);
    }

    public Sweet updateSweet(String id, Sweet sweet) {
        if (sweetRepository.existsById(id)) {
            sweet.setId(id);
            return sweetRepository.save(sweet);
        }
        return null;
    }

    public Optional<Sweet> getSweetById(String id) {
        return sweetRepository.findById(id);
    }

    public boolean deleteSweet(String id) {
        try {
            if (sweetRepository.existsById(id)) {
                sweetRepository.deleteById(id);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting sweet with ID " + id + ": " + e.getMessage());
            return false;
        }
    }

    public boolean purchaseSweet(String sweetId, int quantity) {
        try {
        
            if (quantity <= 0) {
                System.out.println("Invalid quantity: " + quantity);
                return false;
            }
            
            Optional<Sweet> sweetOptional = sweetRepository.findById(sweetId);
            
            if (sweetOptional.isEmpty()) {
                System.out.println("Sweet not found with ID: " + sweetId);
                return false;
            }
            
            Sweet sweet = sweetOptional.get();
           
            if (sweet.getQuantity() < quantity) {
                System.out.println("Insufficient stock. Available: " + sweet.getQuantity() + ", Requested: " + quantity);
                return false;
            }
            
            sweet.setQuantity(sweet.getQuantity() - quantity);
            sweetRepository.save(sweet);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error during purchase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
public boolean restockSweet(String sweetId, int quantity) {
    try {
      
        if (quantity <= 0) {
            System.out.println("Invalid restock quantity: " + quantity);
            return false;
        }
        
        Optional<Sweet> sweetOptional = sweetRepository.findById(sweetId);
        
        if (sweetOptional.isEmpty()) {
            System.out.println("Sweet not found with ID: " + sweetId);
            return false;
        }
        
        Sweet sweet = sweetOptional.get();
        int newQuantity = sweet.getQuantity() + quantity;
        sweet.setQuantity(newQuantity);
        sweetRepository.save(sweet);
        
        return true;
        
    } catch (Exception e) {
        System.err.println("Error during restock: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}
}