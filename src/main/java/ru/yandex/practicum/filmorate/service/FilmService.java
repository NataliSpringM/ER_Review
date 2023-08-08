package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

// сервис для определения рейтинга фильмов
public interface FilmService {

    Film addFilm(Film film);  // добавление информации о фильме в FilmStorage

    Film updateFilm(Film film); // обновление информации о фильме в FilmStorage

    List<Film> listFilms(); // получение списка фильмов из FilmStorage

    Film getFilmById(Integer id); // получение фильма по идентификатору из FilmStorage

    Film addLike(Integer id, Long userId); // добавление лайка фильму в LikeStorage

    Film deleteLike(Integer id, Long userId); // удаление лайка у фильма в LikeStorage

    List<Film> listMostPopularFilms(Integer count); // получение списка наиболее популярных фильмов из UserStorage

}
