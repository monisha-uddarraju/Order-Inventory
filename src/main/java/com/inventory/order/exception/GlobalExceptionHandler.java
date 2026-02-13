//package com.inventory.order.exception;
//
//import org.springframework.http.*;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(Map.of("error", ex.getMessage()));
//    }
//
//    @ExceptionHandler(ResourceAlreadyExistsException.class)
//    public ResponseEntity<?> handleExists(ResourceAlreadyExistsException ex) {
//        return ResponseEntity.status(HttpStatus.CONFLICT)
//                .body(Map.of("error", ex.getMessage()));
//    }
//
//    @ExceptionHandler(BadRequestException.class)
//    public ResponseEntity<?> handleBad(BadRequestException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(Map.of("error", ex.getMessage()));
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors()
//                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
//        return ResponseEntity.badRequest().body(errors);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleOther(Exception ex) {
//        return ResponseEntity.internalServerError()
//                .body(Map.of("error", ex.getMessage()));
//    }
//}
package com.inventory.order.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", 404,
                        "error", "Not Found",
                        "message", ex.getMessage()
                ));
    }

    // 409 (not in spec) → convert to 400 Bad Request as per allowed list
    // Requirement: only 400,401,404,500,505 allowed in errors
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleExists(ResourceAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", ex.getMessage()
                ));
    }

    // 400 - Bad Request
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBad(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", ex.getMessage()
                ));
    }

    // 400 - Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "validationErrors", errors
                ));
    }

    // 505 - HTTP Version Not Supported (rare but included in doc)
    @ExceptionHandler(HttpVersionNotSupportedException.class)
    public ResponseEntity<?> handleVersion(HttpVersionNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.HTTP_VERSION_NOT_SUPPORTED)
                .body(Map.of(
                        "status", 505,
                        "error", "HTTP Version Not Supported",
                        "message", ex.getMessage()
                ));
    }

    // 500 - Internal Server Error (default fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", ex.getMessage()
                ));
    }
}//1