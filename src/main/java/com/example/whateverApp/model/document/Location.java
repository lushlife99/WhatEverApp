package com.example.whateverApp.model.document;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("locations")
public class Location {

    private Double latitude;
    private Double longitude;

}
