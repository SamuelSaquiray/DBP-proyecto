package com.recaudia.exception;
import org.springframework.http.HttpStatus;
public class TenantNotFoundException extends ApiException { public TenantNotFoundException(String message) { super(message, HttpStatus.NOT_FOUND); } }
