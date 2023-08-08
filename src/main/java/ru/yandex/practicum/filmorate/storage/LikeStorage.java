package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

// хранение информации о поставленных лайках

public interface LikeStorage {

    Film addLike(Integer filmId, Long userId); // добавление лайка определенному фильму от пользователя

    Film deleteLike(Integer filmId, Long userId); // удаление лайка у определенного фильма от пользователя

}
