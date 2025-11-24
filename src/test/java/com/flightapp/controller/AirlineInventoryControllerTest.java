package com.flightapp.controller;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.flightapp.dto.InventoryRequestDto;
import com.flightapp.repository.FlightInventoryRepository;
import com.flightapp.repository.FlightRepository;



@SpringBootTest
@AutoConfigureWebTestClient
class AirlineInventoryControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private FlightInventoryRepository inventoryRepo;
    
    @Autowired
    private FlightRepository flightRepo;

    @AfterEach
    void cleanUp() {
    	inventoryRepo.deleteAll().block();
    	flightRepo.deleteAll().block();
    }
    private InventoryRequestDto buildValidDto() {
        InventoryRequestDto inventoryDto = new InventoryRequestDto();
        inventoryDto.setAirlineName("IndiGo");
        inventoryDto.setAirlineLogo("https://indigo/logo.png");
        inventoryDto.setFromPlace("Delhi");
        inventoryDto.setToPlace("Mumbai");
        inventoryDto.setFlightNumber("6E-512");
        inventoryDto.setDepartureTime(LocalDateTime.now().plusDays(1));
        inventoryDto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
        inventoryDto.setPrice(4500);
        inventoryDto.setTotalSeats(180);
        inventoryDto.setAvailableSeats(180);
        return inventoryDto;
    }
    private InventoryRequestDto buildValidDto2() {
        InventoryRequestDto inventoryDto = new InventoryRequestDto();
        inventoryDto.setAirlineName("IndiGo");
        inventoryDto.setAirlineLogo("https://indigo/logo.png");
        inventoryDto.setFromPlace("Delhi");
        inventoryDto.setToPlace("Kolkata");
        inventoryDto.setFlightNumber("6E-512");
        inventoryDto.setDepartureTime(LocalDateTime.now().plusDays(1));
        inventoryDto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
        inventoryDto.setPrice(4500);
        inventoryDto.setTotalSeats(180);
        inventoryDto.setAvailableSeats(180);
        return inventoryDto;
    }
    @Test
    void addInventory_validationError_missingAirlineName() {
        InventoryRequestDto inventoryDto = buildValidDto();
        inventoryDto.setAirlineName("");

        webTestClient.post()
                .uri("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void addInventory_validationError_timeMisMatch() {
        InventoryRequestDto inventoryDto = buildValidDto();
        inventoryDto.setDepartureTime(LocalDateTime.now().plusDays(2));
        inventoryDto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));

        webTestClient.post()
                .uri("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void addInventory_SameSourceDestination() {
        InventoryRequestDto inventoryDto = buildValidDto();
        inventoryDto.setToPlace("Kolkata");
        inventoryDto.setFromPlace("Kolkata");

        webTestClient.post()
                .uri("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void addInventory_InvalidAirport() {
        InventoryRequestDto inventoryDto = buildValidDto();
        inventoryDto.setToPlace("Odisha");

        webTestClient.post()
                .uri("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void addInventory_validationError_availableSeatsGreaterThanTotal() {
        InventoryRequestDto inventoryDto = buildValidDto();
        inventoryDto.setAvailableSeats(300);

        webTestClient.post()
                .uri("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void addInventory_secondTime() {
        InventoryRequestDto inventoryDto = buildValidDto();
        webTestClient.post()
                .uri("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryDto)
                .exchange()
                .expectStatus().isCreated();
        
        webTestClient.post()
		        .uri("/api/flight/airline/inventory/add")
		        .contentType(MediaType.APPLICATION_JSON)
		        .bodyValue(inventoryDto)
		        .exchange()
		        .expectStatus().isBadRequest();
    }
    @Test
    void addInventory_routeMisMatch() {
        InventoryRequestDto inventoryDto = buildValidDto();
        InventoryRequestDto inventoryDto2=buildValidDto2();
        webTestClient.post()
                .uri("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryDto)
                .exchange()
                .expectStatus().isCreated();
        
        webTestClient.post()
		        .uri("/api/flight/airline/inventory/add")
		        .contentType(MediaType.APPLICATION_JSON)
		        .bodyValue(inventoryDto2)
		        .exchange()
		        .expectStatus().isBadRequest();
    }
    @Test
    void addInventory_flightNumberTaken() {
        InventoryRequestDto inventoryDto = buildValidDto();
        InventoryRequestDto inventoryDto2=buildValidDto2();
        webTestClient.post()
                .uri("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryDto)
                .exchange()
                .expectStatus().isCreated();
        
        webTestClient.post()
		        .uri("/api/flight/airline/inventory/add")
		        .contentType(MediaType.APPLICATION_JSON)
		        .bodyValue(inventoryDto2)
		        .exchange()
		        .expectStatus().isBadRequest();
    }
}
