package com.example.whateverApp.model.document;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("locations")
public class Location {

    @Id
    private String _id;

    @NotNull(message = "location must not be null")
    private Double latitude;
    @NotNull(message = "location must not be null")
    private Double longitude;

    public Location(Double latitude, Double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
