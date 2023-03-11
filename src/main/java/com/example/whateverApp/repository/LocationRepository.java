package com.example.whateverApp.repository;

import com.example.whateverApp.model.document.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationRepository extends MongoRepository<Location, String> {
}
