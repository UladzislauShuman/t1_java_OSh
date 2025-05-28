package ru.t1.java.demo.exception;

public class AccountException extends RuntimeException{
    public AccountException() {
        super();
    }
    public AccountException(String message) {
        super(message);
    }
}
