package com.example.whateverApp.model.document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("locations")
public class Location {

    @Id
    private String _id;
    private Double latitude;
    private Double longitude;

    public Location(Double latitude, Double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
