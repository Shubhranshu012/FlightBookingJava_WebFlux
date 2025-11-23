package com.flightapp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class SearchRequestDto {
    private String fromPlace;
    private String toPlace;
    private LocalDate journeyDate;
    private String tripType;   
    private LocalDate returnDate;
}
