package com.flightapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.dto.BookingRequestDto;
import com.flightapp.service.BookingService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
public class BookingController {
	
	@Autowired
	BookingService bookingService;
	
	@PostMapping("/api/flight/booking/{flightId}")
	public Mono<ResponseEntity<Object>> book(@PathVariable String flightId,@RequestBody @Valid BookingRequestDto dto) {

	    return bookingService.bookTicket(flightId, dto).map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
	}
	
	@GetMapping("/api/flight/ticket/{Pnr}")
	public Mono<Object> history(@PathVariable String Pnr){
		return bookingService.getHistory(Pnr);
	}
	
	
	@GetMapping("api/flight/booking/history/{email}")
	public Mono<Object> historyEmail(@PathVariable String email){
		return bookingService.getTicket(email);
	}
	
	@DeleteMapping("/api/flight/booking/cancel/{Pnr}")
	public Mono<Void> deleteBooking(@PathVariable String Pnr){
		return bookingService.cancelTicket(Pnr);
	}
	
}
