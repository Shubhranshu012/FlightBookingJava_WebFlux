package com.flightapp.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import com.flightapp.entity.Flight;

@Repository
public interface FlightRepository extends ReactiveMongoRepository<Flight,String> {
	
}
