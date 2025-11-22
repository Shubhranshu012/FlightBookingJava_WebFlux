package com.flightapp.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import com.flightapp.entity.Booking;

@Repository
public interface BookingRepository extends ReactiveMongoRepository<Booking,String>{

}
