package com.flightapp.controller;

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class BookingController {
	
	private final BookingService bookingService;
	
	public BookingController(BookingService bookingService) { 
		this.bookingService = bookingService;
	}
	@PostMapping("/api/flight/booking/{flightId}")
	public Mono<ResponseEntity<Object>> book(@PathVariable String flightId,@RequestBody @Valid BookingRequestDto bookingDto) {
	    return bookingService.bookTicket(flightId, bookingDto).map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
	}
	
	@GetMapping("/api/flight/ticket/{pnr}")
	public Mono<Object> history(@PathVariable String pnr){
		return bookingService.getHistory(pnr);
	}

	@GetMapping("api/flight/booking/history/{email}")
	public Flux<Object> historyEmail(@PathVariable String email){
		return bookingService.getTicket(email);
	}
	
	@DeleteMapping("/api/flight/booking/cancel/{pnr}")
	public Mono<Void> deleteBooking(@PathVariable String pnr){
		return bookingService.cancelTicket(pnr);
	}
}
