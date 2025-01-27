package dev.riss.notice.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
