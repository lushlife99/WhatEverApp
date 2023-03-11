package com.example.whateverApp.model.document;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("sellerLocations")
public class SellerLocation {

    @Id
    private String id;
    private Integer connectionId;
    private List<Location> locationList;

}
