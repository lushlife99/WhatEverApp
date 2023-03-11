package com.example.whateverApp.model.document;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("locations")
public class Location {

    private Float latitude;
    private Float longitude;

}
