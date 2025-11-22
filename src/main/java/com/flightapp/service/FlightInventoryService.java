package com.flightapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.flightapp.dto.InventoryRequestDto;
import com.flightapp.entity.Airport;
import com.flightapp.entity.Flight;
import com.flightapp.entity.FlightInventory;
import com.flightapp.repository.FlightInventoryRepository;
import com.flightapp.repository.FlightRepository;

import reactor.core.publisher.Mono;

@Service
public class FlightInventoryService {
	
	@Autowired
	FlightInventoryRepository flightInventoryRepo;
	
	@Autowired
	FlightRepository flightRepo;
	
	public Mono<Object> addInventory(InventoryRequestDto inventoryDto) {

	    Airport source = Airport.valueOf(inventoryDto.getFromPlace());
	    Airport destination = Airport.valueOf(inventoryDto.getToPlace());

	    return flightInventoryRepo
	        .findByAirlineAndFlightIdAndSourceAndDestinationAndDepartureTime(inventoryDto.getAirlineName(),inventoryDto.getFlightNumber(),source,destination,inventoryDto.getDepartureTime())
	        .flatMap(existing -> Mono.error(new RuntimeException("Inventory already exists for this flight with same details")))
	        .switchIfEmpty(
	            flightRepo.findById(inventoryDto.getFlightNumber())
	                .flatMap(existingFlight -> {
	                    if (!existingFlight.getAirline().equals(inventoryDto.getAirlineName())) {
	                        return Mono.error(new RuntimeException("Flight Number Is already Associated"));
	                    }
	                    if (!existingFlight.getSource().equals(source) || !existingFlight.getDestination().equals(destination)) {
	                        return Mono.error(new RuntimeException("Flight route mismatch"));
	                    }
	                    return Mono.just(existingFlight);
	                })
	                .switchIfEmpty(Mono.defer(() -> {
	                    Flight newFlight = Flight.builder().id(inventoryDto.getFlightNumber()).airline(inventoryDto.getAirlineName()).source(source).destination(destination).build();
	                    return flightRepo.save(newFlight);
	                }))
	                .flatMap(flight -> {
	                    FlightInventory newInventory = FlightInventory.builder().airline(inventoryDto.getAirlineName()).flightId(flight.getId()).source(source).destination(destination)
	                            .departureTime(inventoryDto.getDepartureTime()).arrivalTime(inventoryDto.getArrivalTime()).price(inventoryDto.getPrice())
	                            .totalSeats(inventoryDto.getTotalSeats()).availableSeats(inventoryDto.getAvailableSeats()).build();
	                    return flightInventoryRepo.save(newInventory);
	                })
	        );
	}


}
