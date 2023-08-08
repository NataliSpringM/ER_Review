package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

//хранение информации о пользователях

public interface UserStorage {

    User addUser(User user);  // добавление информации о пользователе

    User updateUser(User user);  // обновление информации о пользователе

    List<User> listUsers(); // получение списка пользователей

    User getUserById(Long id); // получение пользователя по идентификатору

    void updateUserData(User user); // сохранение новой или обновленной информации о пользователе

    Map<Long, User> getUsersData(); // получение информации о пользователях

}
