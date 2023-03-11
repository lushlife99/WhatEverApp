package com.example.whateverApp.repository;

import com.example.whateverApp.model.entity.LocationConnection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationConnectionRepository extends JpaRepository<LocationConnection, Long> {
}
