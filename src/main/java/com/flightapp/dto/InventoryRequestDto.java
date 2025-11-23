package com.flightapp.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRequestDto {
	
	@NotBlank(message = "Airline name is required")
	private String airlineName;
	private String airlineLogo;
	
	@NotBlank(message = "From place is required")
	private String fromPlace;
	@NotBlank(message = "To place is required")
	private String toPlace;

	@NotBlank(message = "Flight number is required")
	private String flightNumber;

	@NotNull(message = "Departure time is required")
	@Future(message = "Departure time Should Be in Future")
	private LocalDateTime departureTime;

	@NotNull(message = "Arrival time is required")
	@Future(message = "Arrival time Should Be in Future")
	private LocalDateTime arrivalTime;
	
	@NotNull(message = "Price is required")
	@Min(value = 1, message = "Price Must Be more Than 0")
	private float price;
	
	@NotNull(message = "Total Seat is required")
	@Min(value = 0, message = "Total Seat can't be negative")
	private Integer totalSeats;
	
	@NotNull(message = "Available Seats is required")
	@Min(value = 0, message = "Available Seats can't be negative")
	private Integer availableSeats;
}
