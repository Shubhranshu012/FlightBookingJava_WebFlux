package com.flightapp.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;



@Document("flight_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightInventory {
	@Id
	private String id;
	private String airline;
	private String flightId;   //
    private Airport source;   
    private Airport destination;
	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;	
	private float price;		
	private Integer totalSeats;
	private Integer availableSeats;
}
