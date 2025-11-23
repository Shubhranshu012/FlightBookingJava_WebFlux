package com.flightapp.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flightapp.dto.BookingRequestDto;
import com.flightapp.entity.Booking;
import com.flightapp.entity.BookingStatus;
import com.flightapp.entity.Passenger;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightInventoryRepository;
import com.flightapp.repository.PassengerRepository;

import reactor.core.publisher.Mono;

@Service
public class BookingService {
	
	@Autowired
	FlightInventoryRepository flightInventoryRepo;
	
	@Autowired
	BookingRepository bookingRepo;
	
	@Autowired
	PassengerRepository passengerRepo;
	
	public Mono<Object> bookTicket(String flightId, BookingRequestDto dto) {
        if (dto.getSeatNumbers().size() != dto.getPassengers().size()) {
            return Mono.error(new RuntimeException("Seat numbers count must match passenger count"));
        }
        Set<String> uniqueSeats = new HashSet<>(dto.getSeatNumbers());
        if (uniqueSeats.size() != dto.getSeatNumbers().size()) {
            return Mono.error(new RuntimeException("Duplicate seat numbers in request"));
        }
        return flightInventoryRepo.findByFlightId(flightId)
                .switchIfEmpty(Mono.error(new RuntimeException("Flight not found")))
                .flatMap(flightInventory -> passengerRepo.findSeatNumbersByFlightInventoryId(flightInventory.getId()).collectList()
                        .flatMap(bookedSeats -> {
                            if (flightInventory.getAvailableSeats() < dto.getNumberOfSeats()) {
                                return Mono.error(new RuntimeException("Not enough seats available"));
                            }
                            for (String seat : dto.getSeatNumbers()) {
                                if (bookedSeats.contains(seat)) {
                                    return Mono.error(new RuntimeException("Seat " + seat + " already booked"));
                                }
                            }
                            flightInventory.setAvailableSeats(flightInventory.getAvailableSeats() - dto.getNumberOfSeats());

                            return flightInventoryRepo.save(flightInventory)
                                    .flatMap(savedFlight -> {
                                        String pnr = "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                                        Booking booking = Booking.builder().pnr(pnr).email(dto.getEmail()).bookingTime(LocalDateTime.now()).departureTime(savedFlight.getDepartureTime())
                                                .arrivalTime(savedFlight.getArrivalTime()).flightInventoryId(savedFlight.getId()).status(BookingStatus.BOOKED).build();

                                        return bookingRepo.save(booking)
                                                .flatMap(savedBooking -> {
                                                    List<Passenger> passList = dto.getPassengers().stream()
                                                            .map(p -> Passenger.builder().name(p.getName()).gender(p.getGender()).age(p.getAge()).flightInventoryId(savedFlight.getId())
                                                                    .seatNumber(p.getSeatNumber()).mealOption(p.getMealOption()).bookingId(savedBooking.getId()).build())
                                                            .collect(Collectors.toList());

                                                    return passengerRepo.saveAll(passList).then(Mono.just(Map.of("pnr", pnr)));
                                                });
                                    });
                        })
                );
    }

}
