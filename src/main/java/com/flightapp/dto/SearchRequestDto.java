package com.flightapp.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SearchRequestDto {
	@NotBlank(message = "From place is required")
    private String fromPlace;
	
	@NotBlank(message = "To place is required")
    private String toPlace;
	
	@NotNull(message = "Journey date is required")
    private LocalDate journeyDate;
	
	@NotBlank(message = "Trip type is required")
    @Pattern(regexp = "(?i)^(ONE_WAY|ROUND_TRIP)$",message = "Trip type must be ONE_WAY or ROUND_TRIP")
    private String tripType;   
    private LocalDate returnDate;
}
