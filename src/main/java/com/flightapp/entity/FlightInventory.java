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
	private Long id;
	private String flightId; 
	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;	
	private Double price;
	private Integer totalSeats;
	private Integer availableSeats;
}
