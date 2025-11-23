package com.flightapp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document("passengers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Passenger {

    @Id
    private String id;

    private String name;
    private String gender;
    private Integer age;
    private String seatNumber;
    private String mealOption;

    private String bookingId;  
    private String flightInventoryId;
    private BookingStatus status;
}
