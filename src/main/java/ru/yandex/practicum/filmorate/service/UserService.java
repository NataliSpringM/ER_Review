package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

// сервис для добавления, удаления, получение списков друзей пользователя
public interface UserService {
    User addUser(User user);  // добавление информации о пользователе в UserStorage

    User updateUser(User user);  // обновление информации о пользователе в UserStorage

    List<User> listUsers(); // получение списка пользователей из UserStorage

    User getUserById(Long id); // получение пользователя по идентификатору из UserStorage

    User addFriend(Long userId, Long friendId); //добавление пользователя в список друзей в UserStorage

    User deleteFriend(Long userId, Long friendId);  // удаление пользователя из списка друзей в UserStorage

    List<User> listUserFriends(Long id); // получение списка друзей пользователя из UserStorage

    List<User> listCommonFriends(Long userId, Long otherId); // получение списка общих друзей


}

