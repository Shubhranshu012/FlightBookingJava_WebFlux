package com.flightapp.dto;

import lombok.*;

@Data
public class PassengerDto {
    private String name;
    private String gender;
    private Integer age;
    private String seatNumber;
    private String mealOption;
}