package com.flightapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.dto.InventoryRequestDto;
import com.flightapp.service.FlightInventoryService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
public class AirlineInventoryController {
	
	@Autowired
	FlightInventoryService flightService;
	
	@PostMapping("/api/flight/airline/inventory/add")
	public Mono<ResponseEntity<Void>> addInventory(@RequestBody @Valid InventoryRequestDto inventorydto) {
		
	    return flightService.addInventory(inventorydto).then(Mono.just(ResponseEntity.status(201).build()));
	}

}
