package ru.yandex.practicum.filmorate.exceptions;

public class FilmDoesNotExistException extends RuntimeException {

    public FilmDoesNotExistException(String message) {
        super(message);
    }

    public FilmDoesNotExistException(String message, Integer id) {
        super(message);
    }
}
