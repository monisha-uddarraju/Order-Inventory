package com.order.inventory.exception;
 
import org.springframework.http.*;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.*;
 
import io.swagger.v3.oas.annotations.Hidden;
 
import java.time.Instant;

import java.util.Map;
 
@Hidden

@RestControllerAdvice

public class GlobalExceptionHandler {
 
    @ExceptionHandler(NotFoundException.class)

    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("404", ex.getMessage()));

    }
 
    @ExceptionHandler(BadRequestException.class)

    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error("400", ex.getMessage()));

    }
 
    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error("400", "Validation failed"));

    }
 
//    @ExceptionHandler(Exception.class)

//    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {

//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

//                .body(error("500", "An internal server error occurred."));

//    }

    @ExceptionHandler(Exception.class)

    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {

        // TEMP: log full stack so we can see the root cause in console

        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

            .body(error("500", ex.getClass().getName() + ": " + ex.getMessage()));

    }
 
    private Map<String, Object> error(String code, String message) {

        return Map.of("code", code, "error", message, "timestamp", Instant.now().toString());

    }

}
 