package com.flightapp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document("flights")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class Flight {
	@Id
    private String id;
    private String airline;
    private Airport source;       
    private Airport destination;  
}
