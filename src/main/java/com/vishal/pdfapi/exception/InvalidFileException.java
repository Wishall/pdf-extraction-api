package com.vishal.pdfapi.exception;

// Used for general client-side file errors: empty, wrong type, or size check within service limits.
public class InvalidFileException extends RuntimeException {
  public InvalidFileException(String message) {
    super(message);
  }
}