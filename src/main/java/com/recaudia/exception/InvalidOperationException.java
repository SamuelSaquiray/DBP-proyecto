package com.recaudia.exception;
import org.springframework.http.HttpStatus;
public class InvalidOperationException extends ApiException {
    public InvalidOperationException(String message) { super(message, HttpStatus.BAD_REQUEST); }
    public InvalidOperationException(String message, Throwable cause) { super(message, HttpStatus.BAD_REQUEST); initCause(cause); }
}
