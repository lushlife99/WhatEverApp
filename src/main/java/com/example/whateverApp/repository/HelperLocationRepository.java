package com.example.whateverApp.repository;

import com.example.whateverApp.model.document.HelperLocation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HelperLocationRepository extends MongoRepository<HelperLocation, String> {
}
