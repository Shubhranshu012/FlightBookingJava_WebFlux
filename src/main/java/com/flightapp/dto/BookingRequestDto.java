package com.flightapp.dto;

import java.util.List;
import lombok.*;

@Data
public class BookingRequestDto {
    private String email;
    private Integer numberOfSeats;
    private List<PassengerDto> passengers;
    private String mealOption;
    private List<String> seatNumbers;
}

