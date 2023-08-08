package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

// хранение информации о фильмах

public interface FilmStorage {

    Film addFilm(Film film);  //добавление информации о фильме

    Film updateFilm(Film film); // обновление информации о фильме

    List<Film> listFilms(); // получение списка фильмов

    Film getFilmById(Integer id); // получение фильма по идентификатору

    void updateFilmData(Film film); // сохранение новой или обновленной информации о фильме

    Map<Integer, Film> getFilmsData(); // получение данных о фильмах


}
