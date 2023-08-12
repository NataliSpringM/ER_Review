package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

@Validated
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class FIlmGenre {

    Integer id;
    Genre name;
}
