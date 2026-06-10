package com.sweetshop.sweetshop_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sweetshop.sweetshop_backend.model.Sweet;
import com.sweetshop.sweetshop_backend.service.SweetService;
import com.sweetshop.sweetshop_backend.util.JwtUtil;

@RestController
@RequestMapping("/api/sweets")
@CrossOrigin(origins = "http://localhost:4200")
public class SweetController {

    private final SweetService sweetService;
    private final JwtUtil jwtUtil;

    public SweetController(SweetService sweetService, JwtUtil jwtUtil) {
        this.sweetService = sweetService;
        this.jwtUtil = jwtUtil;
    }

    // Existing endpoints...
    @PostMapping(value = {"", "/"})
    public ResponseEntity<Sweet> createSweet(@RequestBody Sweet sweet) {
        try {
            Sweet createdSweet = sweetService.createSweet(sweet);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSweet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<Sweet>> getAllSweets() {
        try {
            List<Sweet> sweets = sweetService.getAllSweets();
            return ResponseEntity.ok(sweets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Sweet>> searchSweets(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        
        try {
            if (name == null && category == null && minPrice == null && maxPrice == null) {
                return ResponseEntity.badRequest().build();
            }

            if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                return ResponseEntity.badRequest().build();
            }

            List<Sweet> results;
            if (name != null && category == null && minPrice == null && maxPrice == null) {
                results = sweetService.searchSweetsByName(name);
            } else if (name == null && category != null && minPrice == null && maxPrice == null) {
                results = sweetService.searchSweetsByCategory(category);
            } else if (name == null && category == null && minPrice != null && maxPrice != null) {
                results = sweetService.searchSweetsByPriceRange(minPrice, maxPrice);
            } else {
                results = sweetService.searchSweets(name, category, minPrice, maxPrice);
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sweet> updateSweet(@PathVariable String id, @RequestBody Sweet sweet) {
        try {
            if (sweet.getName() == null || sweet.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (sweet.getPrice() < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (sweet.getQuantity() < 0) {
                return ResponseEntity.badRequest().build();
            }

            Sweet updatedSweet = sweetService.updateSweet(id, sweet);
            
            if (updatedSweet != null) {
                return ResponseEntity.ok(updatedSweet);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSweet(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
        
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = authHeader.substring(7);
        
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);
            
            if (!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            boolean deleted = sweetService.deleteSweet(id);
            
            if (deleted) {
                return ResponseEntity.noContent().build(); 
            } else {
                return ResponseEntity.notFound().build(); 
            }

        } catch (Exception e) {
            System.err.println("Error in deleteSweet endpoint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


@PostMapping("/{id}/purchase")
public ResponseEntity<Map<String, Object>> purchaseSweet(
        @PathVariable String id,
        @RequestBody Map<String, Object> request) {
    try {
        Integer quantity = (Integer) request.get("quantity");
        
        if (quantity == null || quantity <= 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Quantity must be greater than 0");
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean purchaseResult = sweetService.purchaseSweet(id, quantity);
        
        Map<String, Object> response = new HashMap<>();
        if (purchaseResult) {
            response.put("message", "Purchase successful");
            response.put("success", true);
            response.put("sweetId", id);
            response.put("quantity", quantity);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Purchase failed - Sweet not found or insufficient stock");
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }
        
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Purchase failed");
        errorResponse.put("success", false);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

@PostMapping("/{id}/restock")
public ResponseEntity<Map<String, Object>> restockSweet(
        @PathVariable String id,
        @RequestBody Map<String, Object> request,
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
    
    try {
    
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Admin authorization required");
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        String token = authHeader.substring(7);
   
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        
        if (!jwtUtil.validateToken(token, username)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token");
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        if (!"ADMIN".equals(role)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Admin role required for restocking");
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        Integer quantity = (Integer) request.get("quantity");
        
        if (quantity == null || quantity <= 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Restock quantity must be greater than 0");
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean restockResult = sweetService.restockSweet(id, quantity);
        
        Map<String, Object> response = new HashMap<>();
        if (restockResult) {
            response.put("message", "Restock successful");
            response.put("success", true);
            response.put("sweetId", id);
            response.put("restockedQuantity", quantity);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Restock failed - Sweet not found");
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }
        
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Restock failed");
        errorResponse.put("success", false);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
}