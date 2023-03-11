package com.example.whateverApp.repository;

import com.example.whateverApp.model.document.SellerLocation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SellerLocationRepository extends MongoRepository<SellerLocation, String> {
}
