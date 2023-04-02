package com.example.whateverApp.model.document;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("helperLocations")
public class HelperLocation {

    @Id
    private String _id;
    private Integer connectionId;
    private List<Location> locationList;

}
