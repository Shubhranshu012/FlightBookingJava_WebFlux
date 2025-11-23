package com.flightapp.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleValidationErrors(WebExchangeBindException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return Mono.just(ResponseEntity.badRequest().body(errors));
    }
	 @ExceptionHandler(BadRequestException.class)
	 @ResponseStatus(HttpStatus.BAD_REQUEST)
	 public Mono<Map<String, String>> handleRuntime(BadRequestException ex) {	
		 Map<String, String> error = new HashMap<>();
	     error.put("message", ex.getMessage());
	     return Mono.just(error);
	 }
	 
	 @ExceptionHandler(NotFoundException.class)
	 @ResponseStatus(HttpStatus.NOT_FOUND)
	 public Mono<Void> handleRuntime(NotFoundException ex) {
	     return null;
	 }
}
