package com.flightapp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.entity.Passenger;

import reactor.core.publisher.Flux;

@Repository
public interface PassengerRepository extends ReactiveMongoRepository<Passenger, String> {

    Flux<Passenger> findByBookingId(String bookingId);
    
    @Aggregation(pipeline = {
    	    "{ '$match': { 'flightInventoryId': ?0 }}",
    	    "{ '$project': { '_id': 0, 'seatNumber': 1 }}"
    	})
    Flux<String> findSeatNumbersByFlightInventoryId(String flightInventoryId);
}