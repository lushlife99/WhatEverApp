package com.example.whateverApp.model.document;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document("helperLocations")
@Builder
public class HelperLocation {

    @Id
    private String _id;
    private Long workId;
    private List<Location> locationList = new ArrayList<Location>();

}
