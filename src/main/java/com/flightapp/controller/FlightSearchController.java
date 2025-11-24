package com.flightapp.controller;

import reactor.core.publisher.Mono;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.flightapp.dto.SearchRequestDto;
import com.flightapp.entity.FlightInventory;
import com.flightapp.service.FlightInventoryService;
import java.util.List;
import java.util.Map;

@RestController
public class FlightSearchController {
	
    private final FlightInventoryService inventoryService;
	
	private FlightSearchController(FlightInventoryService inventoryService) {
		this.inventoryService=inventoryService;
	}
	
    @PostMapping("/api/flight/search")
    public ResponseEntity<Mono<Map<String, List<FlightInventory>>>> search(@RequestBody SearchRequestDto dto) {
        return ResponseEntity.ok(inventoryService.searchFlights(dto));
 
    }
}