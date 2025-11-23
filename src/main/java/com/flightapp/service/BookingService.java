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
import com.flightapp.exception.BadRequestException;
import com.flightapp.exception.NotFoundException;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightInventoryRepository;
import com.flightapp.repository.PassengerRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookingService {
	
	@Autowired
	FlightInventoryRepository flightInventoryRepo;
	
	@Autowired
	BookingRepository bookingRepo;
	
	@Autowired
	PassengerRepository passengerRepo;
	
	public Mono<Object> bookTicket(String flightId, BookingRequestDto bookingDto) {
        if (bookingDto.getNumberOfSeats() != bookingDto.getPassengers().size()) {
            return Mono.error(new BadRequestException("Seat numbers count must match passenger count"));
        }
        System.out.println(bookingDto.getSeatNumbers());
        System.out.println(bookingDto.getSeatNumbers().size());
        Set<String> uniqueSeats = new HashSet<>(bookingDto.getSeatNumbers());
        if (uniqueSeats.size() != bookingDto.getSeatNumbers().size()) {
            return Mono.error(new BadRequestException("Duplicate seat numbers in request"));
        }
        return flightInventoryRepo.findByFlightId(flightId)
                .switchIfEmpty(Mono.error(new BadRequestException("Flight not found")))
                .flatMap(flightInventory -> passengerRepo.findSeatNumbersByFlightInventoryId(flightInventory.getId()).collectList()
                		.flatMap(bookedSeats -> {
                            if (flightInventory.getAvailableSeats() < bookingDto.getNumberOfSeats()) {
                                return Mono.error(new BadRequestException("Not enough seats available"));
                            }
                            for (String seat : bookingDto.getSeatNumbers()) {
                                if (bookedSeats.contains(seat)) {
                                    return Mono.error(new BadRequestException("Seat " + seat + " already booked"));
                                }
                            }
                            flightInventory.setAvailableSeats(flightInventory.getAvailableSeats() - bookingDto.getNumberOfSeats());

                            return flightInventoryRepo.save(flightInventory)
                                    .flatMap(savedFlight -> {
                                        String pnr = "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                                        Booking booking = Booking.builder().pnr(pnr).email(bookingDto.getEmail()).bookingTime(LocalDateTime.now()).departureTime(savedFlight.getDepartureTime())
                                                .arrivalTime(savedFlight.getArrivalTime()).flightInventoryId(savedFlight.getId()).status(BookingStatus.BOOKED).build();

                                        return bookingRepo.save(booking)
                                                .flatMap(savedBooking -> {
                                                    List<Passenger> passList = bookingDto.getPassengers().stream()
                                                            .map(p -> Passenger.builder().name(p.getName()).gender(p.getGender()).age(p.getAge()).flightInventoryId(savedFlight.getId())
                                                                    .seatNumber(p.getSeatNumber()).mealOption(p.getMealOption()).bookingId(savedBooking.getId()).status(BookingStatus.BOOKED).build())
                                                            .collect(Collectors.toList());

                                                    return passengerRepo.saveAll(passList).then(Mono.just(Map.of("pnr", pnr)));
                                                });
                                    });
                        })
                );
    }
	
	public Mono<Object> getHistory(String pnr) { 

		return bookingRepo.findByPnrAndStatus(pnr, BookingStatus.BOOKED)
		        .switchIfEmpty(Mono.error(new NotFoundException()))
		        .flatMap(booking ->
		                passengerRepo.findByBookingId(booking.getId()).collectList()
		                        .map(passengers -> Map.of("booking", booking,"passengers", passengers))
		        );
	}
	
	public Flux<Object> getTicket(String email) {

	    return bookingRepo.findByEmailAndStatus(email, BookingStatus.BOOKED)
	            .switchIfEmpty(Mono.error(new NotFoundException()))
	            .flatMap(booking ->
	                    passengerRepo.findByBookingId(booking.getId())
	                            .collectList().map(passengers -> Map.of("booking", booking,"passengers", passengers))
	            );
	}
	
	public Mono<Void> cancelTicket(String pnr) {

	    return bookingRepo.findByPnrAndStatus(pnr, BookingStatus.BOOKED)
	            .switchIfEmpty(Mono.error(new NotFoundException()))
	            .flatMap(booking -> {
	                LocalDateTime now = LocalDateTime.now();
	                if (!booking.getDepartureTime().isAfter(now.plusHours(24))) {
	                    return Mono.error(new BadRequestException("Cannot cancel within 24 hours of journey"));
	                }
	                booking.setStatus(BookingStatus.CANCELLED);

	                return bookingRepo.save(booking)
	                		.then(passengerRepo.findByBookingId(booking.getId()).collectList()
	                				.flatMap(passengers -> {
	                                    passengers.forEach(p -> p.setStatus(BookingStatus.CANCELLED));
	                                    int cancelledSeatCount = passengers.size();
	                                    return passengerRepo.saveAll(passengers).then(Mono.just(cancelledSeatCount)); 
	                                })
	                        )
	                        .flatMap(cancelledCount ->
	                                flightInventoryRepo.findById(booking.getFlightInventoryId())
	                                        .flatMap(flightInventory -> {
	                                            flightInventory.setAvailableSeats(flightInventory.getAvailableSeats() + cancelledCount);
	                                            return flightInventoryRepo.save(flightInventory);
	                                        })
	                        )
	                        .then();
	            });
	}


}
