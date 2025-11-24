package com.flightapp.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.flightapp.dto.InventoryRequestDto;
import com.flightapp.dto.SearchRequestDto;
import com.flightapp.entity.Airport;
import com.flightapp.entity.Flight;
import com.flightapp.entity.FlightInventory;
import com.flightapp.exception.BadRequestException;
import com.flightapp.exception.NotFoundException;
import com.flightapp.repository.FlightInventoryRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.util.AirportUtil;
import reactor.core.publisher.Mono;

@Service
public class FlightInventoryService {
	private final FlightInventoryRepository flightInventoryRepo;
    private final FlightRepository flightRepo;

    public FlightInventoryService(FlightInventoryRepository flightInventoryRepo, FlightRepository flightRepo) {
        this.flightInventoryRepo = flightInventoryRepo;
        this.flightRepo = flightRepo;
    }
    
	public Mono<Object> addInventory(InventoryRequestDto inventoryDto) {
		if (inventoryDto.getAvailableSeats() > inventoryDto.getTotalSeats()) {
            return Mono.error(new BadRequestException("Available seats cannot be greater than total seats"));
        }
    	if (inventoryDto.getArrivalTime().isBefore(inventoryDto.getDepartureTime())) {
    	    return Mono.error(new BadRequestException("Arrival time cannot be before departure time"));
    	}
    	if(inventoryDto.getFromPlace().equals(inventoryDto.getToPlace())) {
    		return Mono.error(new BadRequestException("source and Destination Can't be Same"));
    	}

    	Airport source;
    	Airport destination;
		try {
			source = AirportUtil.validateAirport(inventoryDto.getFromPlace());
			destination = AirportUtil.validateAirport(inventoryDto.getToPlace());
		} 
		catch (BadRequestException e) {
			return Mono.error(new BadRequestException("Source or Deatination invalid"));
		}
    	

	    return flightInventoryRepo
	        .findByAirlineAndFlightIdAndSourceAndDestinationAndDepartureTime(inventoryDto.getAirlineName(),inventoryDto.getFlightNumber(),source,destination,inventoryDto.getDepartureTime())
	        .flatMap(existing -> Mono.error(new BadRequestException("Inventory already exists same details")))
	        .switchIfEmpty(
	            flightRepo.findById(inventoryDto.getFlightNumber())
	                .flatMap(existingFlight -> {
	                    if (!existingFlight.getAirline().equals(inventoryDto.getAirlineName())) {
	                        return Mono.error(new BadRequestException("Flight Number Taken"));
	                    }
	                    if (!existingFlight.getSource().equals(source) || !existingFlight.getDestination().equals(destination)) {
	                        return Mono.error(new BadRequestException("Flight route mismatch"));
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
	
	
	public Mono<Map<String, List<FlightInventory>>> searchFlights(SearchRequestDto searchDto) {

	    LocalDateTime onwardStart = searchDto.getJourneyDate().atStartOfDay();
	    LocalDateTime onwardEnd = searchDto.getJourneyDate().atTime(23, 59, 59);
	    Airport source;
    	Airport destination;
		try {
			source = AirportUtil.validateAirport(searchDto.getFromPlace());
			destination = AirportUtil.validateAirport(searchDto.getToPlace());
		} 
		catch (BadRequestException e) {
			return Mono.error(new BadRequestException("Source or Deatination Invalid"));
		}
	    
	    Mono<List<FlightInventory>> onwardFlightsMono =
	            flightInventoryRepo.findBySourceAndDestinationAndDepartureTimeBetween(source,destination,onwardStart,onwardEnd)
	            .collectList()
	            .flatMap(flights -> {
	                if (flights.isEmpty()) {
	                    return Mono.error(new NotFoundException());
	                }
	                return Mono.just(flights);
	            });

	    if (searchDto.getTripType().toUpperCase().equals("ROUND_TRIP")) {

	        if (searchDto.getReturnDate() == null) {
	            return Mono.error(new BadRequestException("Return date is required"));
	        }
	        if(searchDto.getJourneyDate().isAfter(searchDto.getReturnDate())) {
	        	return Mono.error(new BadRequestException("Return date mush be after Start date"));
	        }
	        LocalDateTime returnStart = searchDto.getReturnDate().atStartOfDay();
	        LocalDateTime returnEnd = searchDto.getReturnDate().atTime(23, 59, 59);

	        Mono<List<FlightInventory>> returnFlightsMono =
	                flightInventoryRepo.findBySourceAndDestinationAndDepartureTimeBetween(source,destination,returnStart,returnEnd)
	                .collectList()
	                .flatMap(list -> {
	                    if (list.isEmpty()) {
	                        return Mono.error(new NotFoundException());
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
