package com.flightapp.util;

import com.flightapp.entity.Gender;
import com.flightapp.exception.BadRequestException;

public class GenderUtil {
	
	private GenderUtil() {
	        
	}
	public static Gender validateGender(String value) throws BadRequestException {
        try {
            return Gender.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Gender Invalid");
        }
    }
}
