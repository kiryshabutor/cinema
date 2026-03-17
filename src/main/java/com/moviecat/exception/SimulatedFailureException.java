package com.moviecat.exception;

import org.springframework.http.HttpStatus;

public class SimulatedFailureException extends ApiException {

    public SimulatedFailureException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
