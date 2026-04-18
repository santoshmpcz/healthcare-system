package com.app.exception;

public class SlotRequestNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SlotRequestNotFoundException() {
		super();
	}

	public SlotRequestNotFoundException(String message) {
		super(message);
	}
}