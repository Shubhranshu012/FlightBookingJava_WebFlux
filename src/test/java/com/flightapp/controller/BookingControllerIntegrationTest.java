package com.flightapp.controller;

import com.flightapp.dto.BookingRequestDto;
import com.flightapp.dto.PassengerDto;
import com.flightapp.entity.Airport;
import com.flightapp.entity.Flight;
import com.flightapp.entity.FlightInventory;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightInventoryRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.repository.PassengerRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@AutoConfigureWebTestClient
class BookingControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FlightRepository flightRepo;

    @Autowired
    private FlightInventoryRepository inventoryRepo;

    @Autowired
    private BookingRepository bookingRepo;
    
    @Autowired
    private PassengerRepository passengerRepo;
    
    private String inventoryId1;
    private String inventoryId2;

    @BeforeEach
    void setup() {
        flightRepo.save(Flight.builder().id("IN-100").airline("IndiGo").source(Airport.DELHI).destination(Airport.MUMBAI).build()).block();
        flightRepo.save(Flight.builder().id("IN-101").airline("IndiGo").source(Airport.MUMBAI).destination(Airport.DELHI).build()).block();

        inventoryRepo.save(FlightInventory.builder().airline("IndiGo").flightId("IN-100")
                        .source(Airport.DELHI).destination(Airport.MUMBAI).departureTime(LocalDateTime.now().plusDays(2))
                        .arrivalTime(LocalDateTime.now().plusDays(2).plusHours(2)).price(4500).totalSeats(180).availableSeats(180).build()).block();

        inventoryRepo.save(FlightInventory.builder().airline("IndiGo").flightId("IN-101")
                        .source(Airport.MUMBAI).destination(Airport.DELHI).departureTime(LocalDateTime.now().plusDays(2))
                        .arrivalTime(LocalDateTime.now().plusDays(2).plusHours(2)).price(4500).totalSeats(180).availableSeats(1).build()).block();
        inventoryId1 = "IN-100";
        inventoryId2 = "IN-101";
    }

    @AfterEach
    void cleanUp() {
    	inventoryRepo.deleteAll().block();
    	flightRepo.deleteAll().block();
    	bookingRepo.deleteAll().block();
    	passengerRepo.deleteAll().block();
    	
    }
    private PassengerDto buildPassenger(String name, String gender, int age, String seat, String meal) {
        PassengerDto passenger = new PassengerDto();
        passenger.setName(name);
        passenger.setGender(gender);
        passenger.setAge(age);
        passenger.setSeatNumber(seat);
        passenger.setMealOption(meal);
        return passenger;
    }

    private BookingRequestDto buildBooking(PassengerDto passenger, List<String> seats) {
        BookingRequestDto booking = new BookingRequestDto();
        booking.setEmail("test@gmail.com");
        booking.setNumberOfSeats(seats.size());
        booking.setSeatNumbers(seats);
        booking.setPassengers(List.of(passenger));
        booking.setMealOption("Mix");
        return booking;
    }

    private BookingRequestDto validBooking() {
        return buildBooking(buildPassenger("Rohit", "MALE", 28, "12A", "VEG"),List.of("12A"));
    }
    
    @Test
    void bookTicket_success() {
        webTestClient.post()
                .uri("/api/flight/booking/" + inventoryId1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validBooking())
                .exchange()
                .expectStatus().isCreated();
    }
    @Test
    void bookTicket_moreSeatsThanPassengers() {
        BookingRequestDto bookingDto = buildBooking(
                buildPassenger("Rohit", "MALE", 28, "12A", "VEG"),
                List.of("1A", "1B")
        );
       
        webTestClient.post()
                .uri("/api/flight/booking/" + inventoryId1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookingDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void bookTicket_GenderInvalid() {
        BookingRequestDto bookingDto = buildBooking(
                buildPassenger("Rohit", "Happy", 28, "12A", "VEG"),
                List.of("1A", "1B")
        );

        webTestClient.post()
                .uri("/api/flight/booking/" + inventoryId1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookingDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void bookTicket_seatAlreadyBooked() {
        webTestClient.post()
                .uri("/api/flight/booking/" + inventoryId1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validBooking())
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/flight/booking/" + inventoryId1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validBooking())
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void bookTicket_duplicateSeatInRequest() {
        PassengerDto passengerDto = buildPassenger("Rohit", "MALE", 28, "12A", "VEG");
        BookingRequestDto bookingDto = buildBooking(passengerDto, List.of("12A", "12A"));
        PassengerDto passengerDto1=buildPassenger("Rohit", "MALE", 28, "12A", "VEG");
        PassengerDto passengerDto2=buildPassenger("Rohit", "MALE", 28, "12B", "VEG");
        bookingDto.setPassengers(List.of(passengerDto1,passengerDto2));

        webTestClient.post()
                .uri("/api/flight/booking/" + inventoryId1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookingDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void bookTicket_notEnoughSeatsLeft() {
        PassengerDto passengerDto = buildPassenger("Rohit", "MALE", 28, "12A", "VEG");
        BookingRequestDto bookingDto = buildBooking(passengerDto, List.of("12A", "12B"));

        webTestClient.post()
                .uri("/api/flight/booking/" + inventoryId2)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookingDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void bookingHistory_empty() {
        webTestClient.get()
                .uri("/api/flight/booking/history/test@gmail.com")
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void cancelBooking_invalidPNR() {
        webTestClient.delete()
                .uri("/api/flight/booking/cancel/PNR0000")
                .exchange()
                .expectStatus().isNotFound();
    }
}
