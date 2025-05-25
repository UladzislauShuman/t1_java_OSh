package ru.t1.java.demo.exception;

public class ClientException extends RuntimeException {
    public ClientException() {
        super();
    }
    public ClientException(String message) {
        super(message);
    }
}
