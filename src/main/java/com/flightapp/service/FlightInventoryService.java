package com.flightapp.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.flightapp.dto.InventoryRequestDto;
import com.flightapp.dto.SearchRequestDto;
import com.flightapp.entity.Airport;
import com.flightapp.entity.Flight;
import com.flightapp.entity.FlightInventory;
import com.flightapp.repository.FlightInventoryRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.util.AirportUtil;
import reactor.core.publisher.Mono;

@Service
public class FlightInventoryService {
	
	@Autowired
	FlightInventoryRepository flightInventoryRepo;
	
	@Autowired
	FlightRepository flightRepo;
	
	@Autowired
	AirportUtil airportUtil;
	public Mono<Object> addInventory(InventoryRequestDto inventoryDto) {
		if (inventoryDto.getAvailableSeats() > inventoryDto.getTotalSeats()) {
            throw new RuntimeException("Available seats cannot be greater than total seats");
        }
    	if (inventoryDto.getArrivalTime().isBefore(inventoryDto.getDepartureTime())) {
    	    throw new RuntimeException("Arrival time cannot be before departure time");
    	}
    	if(inventoryDto.getFromPlace()==inventoryDto.getToPlace()) {
    		throw new RuntimeException("source and Destination Can't be Same");
    	}

    	Airport source = airportUtil.validateAirport(inventoryDto.getFromPlace());
    	Airport destination = airportUtil.validateAirport(inventoryDto.getToPlace());

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
	
	
	public Mono<Map<String, List<FlightInventory>>> searchFlights(SearchRequestDto dto) {

	    LocalDateTime onwardStart = dto.getJourneyDate().atStartOfDay();
	    LocalDateTime onwardEnd = dto.getJourneyDate().atTime(23, 59, 59);
	    Airport source = airportUtil.validateAirport(dto.getFromPlace());
    	Airport destination = airportUtil.validateAirport(dto.getToPlace());
	    
	    Mono<List<FlightInventory>> onwardFlightsMono =
	            flightInventoryRepo.findBySourceAndDestinationAndDepartureTimeBetween(source,destination,onwardStart,onwardEnd)
	            .collectList()
	            .flatMap(flights -> {
	                if (flights.isEmpty()) {
	                    return Mono.error(new RuntimeException("No onward flights found"));
	                }
	                return Mono.just(flights);
	            });

	    if (dto.getTripType().toUpperCase().equals("ROUND_TRIP")) {

	        if (dto.getReturnDate() == null) {
	            return Mono.error(new RuntimeException("Return date is required"));
	        }
	        LocalDateTime returnStart = dto.getReturnDate().atStartOfDay();
	        LocalDateTime returnEnd = dto.getReturnDate().atTime(23, 59, 59);

	        Mono<List<FlightInventory>> returnFlightsMono =
	                flightInventoryRepo.findBySourceAndDestinationAndDepartureTimeBetween(source,destination,returnStart,returnEnd)
	                .collectList()
	                .flatMap(list -> {
	                    if (list.isEmpty()) {
	                        return Mono.error(new RuntimeException("No return flights found"));
	                    }
	                    return Mono.just(list);
	                });

	        return onwardFlightsMono.flatMap(onwardFlights ->
	                returnFlightsMono.map(returnFlights -> {
	                    Map<String, List<FlightInventory>> result = new HashMap<>();
	                    result.put("onwardFlights", onwardFlights);
	                    result.put("returnFlights", returnFlights);
	                    return result;
	                })
	        );
	    }

	    return onwardFlightsMono.map(onward -> {
	        Map<String, List<FlightInventory>> result = new HashMap<>();
	        result.put("onwardFlights", onward);
	        return result;
	    });
	}


}
