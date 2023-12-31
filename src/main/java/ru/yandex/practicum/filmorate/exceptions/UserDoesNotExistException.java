package ru.yandex.practicum.filmorate.exceptions;

public class UserDoesNotExistException extends RuntimeException {

    public UserDoesNotExistException(String message) {
        super(message);
    }

    public UserDoesNotExistException(String message, Long id) {
        super(message);
    }

}
