package com.yoko.backend.exceptions;

public class UserNotFoundException extends Exception {

  public UserNotFoundException(String message) {
    super(message);
  }
}
