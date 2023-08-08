package ru.yandex.practicum.filmorate.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@RequiredArgsConstructor
public class ErrorResponse { // сообщение об ошибке
    private final String error;

}