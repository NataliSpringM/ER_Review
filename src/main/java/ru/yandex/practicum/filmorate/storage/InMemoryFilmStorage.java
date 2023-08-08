package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    // реализация хранения информации о фильмах в памяти

    private final HashMap<Integer, Film> films = new HashMap<>();
    private Integer nextId = 1;

    // добавление информации о фильме
    @Override
    public Film addFilm(Film film) {

        Film newFilm = film.toBuilder().id(nextId).build();
        nextId++;

        updateFilmData(newFilm); // сохранение информации о фильме
        log.info("Сохранена информация о фильме: {}", film);

        return newFilm;

    }

    // обновление информации о фильме
    @Override
    public Film updateFilm(Film film) {

        if (!films.containsKey(film.getId())) {
            throw new FilmDoesNotExistException("Такого фильма нет в списке.");
        }

        updateFilmData(film); // сохранение обновленной информации
        log.info("Обновлена информация о фильме: {}", film);
        return film;

    }

    // получение списка фильмов
    @Override
    public List<Film> listFilms() {

        List<Film> listFilms = new ArrayList<>(films.values());

        log.info("Количество фильмов в списке: {}", listFilms.size());

        return listFilms;
    }

    // получение фильма по идентификатору
    @Override
    public Film getFilmById(Integer id) {

        return films.values().stream()
                .filter(film -> film.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new FilmDoesNotExistException(String.format("Фильм с id %d не найден", id)));
    }

    // сохранение новой или обновленной информации о фильме
    @Override
    public void updateFilmData(Film film) {
        films.put(film.getId(), film);
    }

    // получение данных о фильмах
    @Override
    public Map<Integer, Film> getFilmsData() {
        return films;
    }

}
