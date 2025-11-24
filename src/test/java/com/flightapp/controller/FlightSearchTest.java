package com.flightapp.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightapp.dto.SearchRequestDto;
import com.flightapp.entity.Airport;
import com.flightapp.entity.Flight;
import com.flightapp.entity.FlightInventory;
import com.flightapp.repository.FlightInventoryRepository;
import com.flightapp.repository.FlightRepository;

@SpringBootTest
@AutoConfigureWebTestClient
class FlightSearchTest {
	
	@Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FlightRepository flightRepo;

    @Autowired
    private FlightInventoryRepository inventoryRepo;

    @BeforeEach
    void setup() {
        inventoryRepo.deleteAll().block();
        flightRepo.deleteAll().block();
    	
        flightRepo.save(Flight.builder().id("IN-100").airline("IndiGo").source(Airport.DELHI).destination(Airport.MUMBAI).build()).block();

        flightRepo.save(Flight.builder().id("IN-101").airline("IndiGo").source(Airport.MUMBAI).destination(Airport.DELHI).build()).block();

        inventoryRepo.save(FlightInventory.builder().airline("IndiGo").flightId("IN-100")
                        .source(Airport.DELHI).destination(Airport.MUMBAI).departureTime(LocalDateTime.now().plusDays(2))
                        .arrivalTime(LocalDateTime.now().plusDays(2).plusHours(2)).price(4500).totalSeats(180).availableSeats(180).build()).block();

        inventoryRepo.save(FlightInventory.builder().airline("IndiGo").flightId("IN-101")
                        .source(Airport.MUMBAI).destination(Airport.DELHI).departureTime(LocalDateTime.now().plusDays(3))
                        .arrivalTime(LocalDateTime.now().plusDays(2).plusHours(2)).price(4500).totalSeats(180).availableSeats(1).build()).block();

    }
    
    @AfterEach
    void cleanUp() {
    	inventoryRepo.deleteAll().block();
    	flightRepo.deleteAll().block();
    }
    	
    SearchRequestDto getRequest() {
    	SearchRequestDto search=new SearchRequestDto();
    	search.setFromPlace("DELHI");
    	search.setToPlace("Mumbai");
    	search.setJourneyDate(LocalDate.now().plusDays(2));
    	search.setTripType("One_Way");
    	return search;
    }
    
    @Test
    void search_success() {
    	webTestClient.post()
		        .uri("/api/flight/search")
		        .contentType(MediaType.APPLICATION_JSON)
		        .bodyValue(getRequest())
		        .exchange()
		        .expectStatus().isOk();
    }
    @Test
    void search_toNotProvided() {
    	SearchRequestDto request=getRequest();
    	request.setToPlace("");
    	
    	webTestClient.post()
		        .uri("/api/flight/search")
		        .contentType(MediaType.APPLICATION_JSON)
		        .bodyValue(request)
		        .exchange()
		        .expectStatus().isBadRequest();
    }
    
    @Test
    void search_ReturnDateNotGiven() {
    	SearchRequestDto request=getRequest();
    	request.setTripType("Round_Trip");
    	
    	webTestClient.post()
		        .uri("/api/flight/search")
		        .contentType(MediaType.APPLICATION_JSON)
		        .bodyValue(request)
		        .exchange()
		        .expectStatus().isBadRequest();
    }
    @Test
    void bothWay_NoReturn() {
    	SearchRequestDto request=getRequest();
    	request.setTripType("Round_Trip");
    	request.setReturnDate(LocalDate.now().plusDays(4));
    	
    	webTestClient.post()
		    	.uri("/api/flight/search")
		        .contentType(MediaType.APPLICATION_JSON)
		        .bodyValue(request)
		        .exchange()
		        .expectStatus().isNotFound();
    }
    
    @Test
    void bothWay_Success() {
    	SearchRequestDto request=getRequest();
    	request.setTripType("Round_Trip");
    	request.setReturnDate(LocalDate.now().plusDays(2));
    	
    	webTestClient.post()
		    	.uri("/api/flight/search")
		        .contentType(MediaType.APPLICATION_JSON)
		        .bodyValue(request)
		        .exchange()
		        .expectStatus().isOk();
    }

}
