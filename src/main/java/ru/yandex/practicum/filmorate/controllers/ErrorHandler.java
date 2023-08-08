package ru.yandex.practicum.filmorate.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exceptions.FilmDoesNotExistException;
import ru.yandex.practicum.filmorate.exceptions.UserDoesNotExistException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice("ru.yandex.practicum.filmorate.controllers")
public class ErrorHandler {

    // обработка выбрасываемых исключений

    // обработка ошибок при запросах с несуществующим идентификатором пользователя / фильма
    @ExceptionHandler({UserDoesNotExistException.class, FilmDoesNotExistException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundId(final RuntimeException e) {

        return new ErrorResponse("Несуществующий id: " + e.getMessage());
    }

    // обработка ошибок при прохождении валидации
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleFailValidation(final RuntimeException e) {

        return new ErrorResponse("Ошибка валидации: " + e.getMessage());
    }

    // обработка непредвиденных ошибок
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownError(final Throwable e) {

        return new ErrorResponse("Произошла непредвиденная ошибка: " + e.getMessage());
    }

}
