package de.unima.ki.anyburl.exceptions;

public class TimeOutException extends RuntimeException {

  private static final long serialVersionUID = 177L;

  public TimeOutException() {}

  public String toString() {
    return "Timeout Exception";
  }
}
