package com.flightapp.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.entity.Airport;
import com.flightapp.entity.FlightInventory;

import reactor.core.publisher.Mono;

@Repository
public interface FlightInventoryRepository extends ReactiveMongoRepository<FlightInventory,String>{

	Mono<FlightInventory> findByAirlineAndFlightIdAndSourceAndDestinationAndDepartureTime(
			String airline,
            String flightId,
            Airport source,
            Airport destination,
            LocalDateTime departureTime
    );
}
