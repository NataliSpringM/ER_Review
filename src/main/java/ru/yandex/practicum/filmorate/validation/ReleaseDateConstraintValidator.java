package ru.yandex.practicum.filmorate.validation;


import ru.yandex.practicum.filmorate.annotations.CheckReleaseDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateConstraintValidator implements ConstraintValidator<CheckReleaseDate, LocalDate> {

    // проверка даты выхода фильма на соответствие требованию быть не ранее определенного временного ограничения
    private static final LocalDate RELEASE_DATE_OLD_LIMIT = LocalDate.of(1895, 12, 28);

    @Override
    public void initialize(CheckReleaseDate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if (localDate == null) {
            return false;
        }
        return !localDate.isBefore(RELEASE_DATE_OLD_LIMIT);
    }
}
