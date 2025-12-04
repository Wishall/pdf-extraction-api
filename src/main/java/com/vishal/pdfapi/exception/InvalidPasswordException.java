package com.vishal.pdfapi.exception;

// Used specifically for password-protected/encrypted PDFs.
public class InvalidPasswordException extends RuntimeException {
  public InvalidPasswordException(String message) {
    super(message);
  }
}