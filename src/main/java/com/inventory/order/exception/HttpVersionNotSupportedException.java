package com.inventory.order.exception;

public class HttpVersionNotSupportedException extends RuntimeException {
    
	private static final long serialVersionUID = 1L;

	public HttpVersionNotSupportedException(String message) {
        super(message);
    }
}
