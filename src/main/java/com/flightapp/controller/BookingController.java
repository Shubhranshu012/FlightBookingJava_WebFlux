package com.flightapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.dto.BookingRequestDto;
import com.flightapp.service.BookingService;
import reactor.core.publisher.Mono;

@RestController
public class BookingController {
	
	@Autowired
	BookingService bookingService;
	
	@PostMapping("/api/flight/booking/{flightId}")
	public Mono<ResponseEntity<Object>> book(@PathVariable String flightId,@RequestBody BookingRequestDto dto) {

	    return bookingService.bookTicket(flightId, dto).map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
	}
	
}
