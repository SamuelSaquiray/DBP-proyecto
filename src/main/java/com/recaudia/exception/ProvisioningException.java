package com.recaudia.exception;
import org.springframework.http.HttpStatus;
public class ProvisioningException extends ApiException {
    public ProvisioningException(String message) { super(message, HttpStatus.INTERNAL_SERVER_ERROR); }
    public ProvisioningException(String message, Throwable cause) { super(message, HttpStatus.INTERNAL_SERVER_ERROR); initCause(cause); }
}
