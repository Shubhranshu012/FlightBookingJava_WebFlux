package com.flightapp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import com.flightapp.entity.Booking;
import com.flightapp.entity.BookingStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BookingRepository extends ReactiveMongoRepository<Booking,String>{
	
	Mono<Booking> findByPnrAndStatus(String pnr, BookingStatus status);
	
	Flux<Booking> findByEmailAndStatus(String email,BookingStatus status);

}
;