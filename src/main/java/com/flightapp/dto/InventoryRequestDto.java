package com.flightapp.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InventoryRequestDto {
	private String airlineName;
	private String airlineLogo;
	private String fromPlace;
	private String toPlace;
	private String flightNumber;
	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;
	private Double price;
	private Integer totalSeats;
	private Integer availableSeats;
}
