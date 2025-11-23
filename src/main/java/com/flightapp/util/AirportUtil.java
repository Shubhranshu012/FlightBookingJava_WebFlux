package com.flightapp.util;

import org.springframework.stereotype.Service;

import com.flightapp.entity.Airport;

@Service
public class AirportUtil {
    public Airport validateAirport(String value) {
        try {
            return Airport.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new RuntimeException("Airport is invalid");
        }
    }
}