package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/films")
@Validated
@RequiredArgsConstructor
public class FilmController {

    /* обработка запросов HTTP-клиентов на добавление, обновление, получение информации о фильмах по адресу
    http://localhost:8080/films */

    private final FilmService filmService;


    // обработка POST-запроса на добавление информации о фильме
    @PostMapping()
    public Film addFilm(@Valid @RequestBody Film film) {

        return filmService.addFilm(film);
    }

    // обработка PUT-запроса на обновление информации о фильме
    @PutMapping()
    public Film updateFilm(@Valid @RequestBody Film film) {

        return filmService.updateFilm(film);
    }

    // обработка GET-запроса на получение фильма по идентификатору
    @GetMapping("/{id}")

    public Film getFilmById(@PathVariable Integer id) {

        return filmService.getFilmById(id);
    }

    // обработка GET-запроса на получение списка всех фильмов
    @GetMapping()
    public List<Film> listFilms() {

        return filmService.listFilms();
    }

    // обработка PUT-запроса на добавление лайка фильму
    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Integer id, @PathVariable Long userId) {

        return filmService.addLike(id, userId);
    }

    // обработка DELETE-запроса на удаление лайка фильму
    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Integer id, @PathVariable Long userId) {

        return filmService.deleteLike(id, userId);
    }

    // обработка GET-запроса на получение списка наиболее популярных фильмов
    @GetMapping("/popular")
    public List<Film> listMostPopularFilms(@RequestParam(required = false, defaultValue = "10") Integer count) {

        return filmService.listMostPopularFilms(count);
    }
}



