package com.flightapp.entity;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document("bookings")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class Booking {

    @Id
    private String id;
    private String pnr;
    private String email;
    private LocalDateTime bookingTime;
    private LocalDateTime departureTime;
    private String flightInventoryId;   
    private List<Passenger> passengers; 
    private BookingStatus status;
}