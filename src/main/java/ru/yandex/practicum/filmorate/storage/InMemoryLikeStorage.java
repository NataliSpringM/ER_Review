package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.Command;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryLikeStorage implements LikeStorage {

    // реализация хранения информации о лайках в памяти

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    // полная информация о лайках - <id фильма, <id пользователя, количество лайков пользователя>>
    // информация о лайках пользователя - не boolean для возможности их дальнейшего подсчета при необходимости
    private final Map<Integer, Map<Long, Long>> likes = new HashMap<>();


    @Override
    public Film addLike(Integer filmId, Long userId) {

        // обновляем количество лайков у фильма
        Film film = updateLikes(filmId, userId, Command.ADD);

        // обновление данных о фильме в хранилище фильмов
        filmStorage.updateFilmData(film);

        log.info("Пользователь {} поставил лайк фильму {}",
                userStorage.getUserById(userId), filmStorage.getFilmById(filmId));

        return film;
    }

    @Override
    public Film deleteLike(Integer filmId, Long userId) {

        // получаем фильм с обновленным количество лайков
        Film film = updateLikes(filmId, userId, Command.DELETE);

        // обновление данных о фильме в хранилище
        filmStorage.updateFilmData(film);

        log.info("Пользователь {} удалил лайк у фильма {}",
                userStorage.getUserById(userId), filmStorage.getFilmById(filmId));

        return film;
    }

    // обновление количества лайков у фильма с сохранением информации о пользователях, поставивших лайки
    private Film updateLikes(Integer filmId, Long userId, Command command) {

        // получаем информацию о лайках определенному фильму (id пользователя / количество лайков)
        Map<Long, Long> filmLikesByUsers = getFilmLikesByUsers(filmId);

        // запрашиваем количество лайков определенного пользователя
        Long numberOfUserLikes = filmLikesByUsers.getOrDefault(userId, 0L);

        switch (command) {
            case ADD:

                // добавляем лайк от пользователя в случае отсутствия лайков
                if (numberOfUserLikes == 0L) {

                    filmLikesByUsers.put(userId, ++numberOfUserLikes);

                    // обновляем общие данные
                    likes.put(filmId, filmLikesByUsers);

                } else {
                    throw new RuntimeException("Вы уже ставили лайк этому фильму");
                }
                break;
            case DELETE:
                if (numberOfUserLikes == 1L) {

                    // удаляем лайк от пользователя
                    filmLikesByUsers.put(userId, --numberOfUserLikes);
                    // обновляем общие данные
                    likes.put(filmId, filmLikesByUsers);
                    break;

                } else {

                    throw new RuntimeException("Вы не ставили лайк этому фильму");
                }
        }

        // возвращаем копию объекта фильм с обновленным количеством лайков
        return filmStorage.getFilmById(filmId).toBuilder()
                .likes(getFilmLikesTotalCount(filmId))
                .build();
    }

    // возвращение информации о лайках определенному фильму от пользователей с проверкой на null
    private Map<Long, Long> getFilmLikesByUsers(Integer filmId) {

        return likes.computeIfAbsent(filmId, map -> new HashMap<>());
    }

    // подсчет лайков определенному фильму от всех пользователей
    private Long getFilmLikesTotalCount(Integer filmId) {

        Map<Long, Long> filmLikesByUsers = getFilmLikesByUsers(filmId);
        return filmLikesByUsers.values().stream() // считаем количество лайков у фильма
                .mapToLong(Long::valueOf)
                .sum();
    }


}
