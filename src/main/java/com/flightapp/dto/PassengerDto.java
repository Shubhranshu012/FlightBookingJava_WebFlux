package com.flightapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
public class PassengerDto {
    @NotBlank(message = "Passenger name is required")
	private String name;
    
    @NotBlank(message = "Gender is required")
    private String gender;
    
    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be >= 1")
    private Integer age;
    
    @NotBlank(message = "Seat number is required")
    private String seatNumber;
    
    @NotBlank(message = "Meal option is required")
    @Pattern(regexp = "(?i)VEG|NON_VEG", message = "Meal option must be VEG or NON_VEG")
    private String mealOption;
}