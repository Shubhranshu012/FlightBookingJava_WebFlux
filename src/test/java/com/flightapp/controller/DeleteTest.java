package com.flightapp.controller;

import com.flightapp.entity.Airport;
import com.flightapp.entity.Booking;
import com.flightapp.entity.BookingStatus;
import com.flightapp.entity.Flight;
import com.flightapp.entity.FlightInventory;
import com.flightapp.entity.Passenger;
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
import org.springframework.test.web.reactive.server.WebTestClient;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureWebTestClient
class DeleteTest {

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

    @BeforeEach
    void setup() {
        bookingRepo.deleteAll().block();
        inventoryRepo.deleteAll().block();
        flightRepo.deleteAll().block();

        flightRepo.save(Flight.builder().id("IN-100").airline("IndiGo").source(Airport.DELHI).destination(Airport.MUMBAI).build()).block();

        inventoryRepo.save(FlightInventory.builder().airline("IndiGo").flightId("IN-100")
                .source(Airport.DELHI).destination(Airport.MUMBAI).departureTime(LocalDateTime.now().plusDays(2))
                .arrivalTime(LocalDateTime.now().plusDays(2).plusHours(2)).price(4500).totalSeats(180).availableSeats(180).build()).block();
        
        inventoryId1 = "IN-100";
    }
    @AfterEach
    void cleanUp() {
    	inventoryRepo.deleteAll().block();
    	flightRepo.deleteAll().block();
    	bookingRepo.deleteAll().block();
    }

    private String createBooking(String inventoryId) {
        String pnr = "PNR123";
        Booking booking = Booking.builder().pnr(pnr).email("test@gmail.com").flightInventoryId(inventoryId).status(BookingStatus.BOOKED).bookingTime(LocalDateTime.now()).departureTime(LocalDateTime.now().plusDays(2)).build();
        Passenger passenger = Passenger.builder().bookingId(booking.getId()).name("Rohit").gender("M").age(28).seatNumber("12A").mealOption("VEG").status(BookingStatus.BOOKED).build();
        passengerRepo.save(passenger).block();
        bookingRepo.save(booking).block();
        return pnr;
    }
    private String createBooking2(String inventoryId) {
        String pnr = "PNR1234";
        Booking booking = Booking.builder().pnr(pnr).email("test@gmail.com").flightInventoryId(inventoryId).status(BookingStatus.BOOKED).bookingTime(LocalDateTime.now()).departureTime(LocalDateTime.now()).build();
        Passenger passenger = Passenger.builder().bookingId(booking.getId()).name("Rohit").gender("M").age(28).seatNumber("12A").mealOption("VEG").status(BookingStatus.BOOKED).build();
        passengerRepo.save(passenger).block();
        bookingRepo.save(booking).block();
        return pnr;
    }
    
    @Test
    void test_delete_Success() {
        String pnr = createBooking(inventoryId1);

        webTestClient.delete()
                .uri("/api/flight/booking/cancel/" + pnr)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void test_cancelTimeLimit() {
        String pnr = createBooking2(inventoryId1);

        webTestClient.delete()
                .uri("/api//flight/booking/cancel/" + pnr)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testBookTicket_cancelThenSearch() {
        String pnr = createBooking(inventoryId1);

        webTestClient.delete()
                .uri("/api/flight/booking/cancel/" + pnr)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/api/flight/ticket/" + pnr)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testBookTicket_cancelWrongPnr() {
    	createBooking(inventoryId1);

        webTestClient.get()
                .uri("/api/flight/ticket/WRONGPNR")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void test_Ticket() {
        String pnr = createBooking(inventoryId1);

        webTestClient.get()
                .uri("/api/flight/ticket/" + pnr)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void test_History() {
    	createBooking(inventoryId1);

        webTestClient.get()
                .uri("/api/flight/booking/history/test@gmail.com")
                .exchange()
                .expectStatus().isOk();
    }
}
