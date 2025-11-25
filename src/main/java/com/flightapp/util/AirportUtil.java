package com.flightapp.util;

import com.flightapp.entity.Airport;
import com.flightapp.exception.BadRequestException;

public class AirportUtil {
	
	private AirportUtil() {
		
	}
    public static Airport validateAirport(String value) throws BadRequestException {
        try {
            return Airport.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Airport is invalid");
        }
    }
}