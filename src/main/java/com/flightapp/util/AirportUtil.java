package com.flightapp.util;

import org.springframework.stereotype.Service;

import com.flightapp.entity.Airport;
import com.flightapp.exception.BadRequestException;

@Service
public class AirportUtil {
    public Airport validateAirport(String value) throws BadRequestException {
        try {
            return Airport.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Airport is invalid");
        }
    }
}