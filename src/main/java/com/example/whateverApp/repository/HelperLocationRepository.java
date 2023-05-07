package com.example.whateverApp.repository;

import com.example.whateverApp.model.document.HelperLocation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface HelperLocationRepository extends MongoRepository<HelperLocation, String> {

    Optional<HelperLocation> findByWorkId(Long workId);
}
