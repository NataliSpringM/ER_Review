package ru.yandex.practicum.filmorate.controllers;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;


@RestController
@Validated
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    /* обработка запросов HTTP-клиентов на добавление, обновление, получение информации о пользователях по адресу
    http://localhost:8080/users */

    private final UserService userService;

    // обработка POST-запроса на добавление данных пользователя
    @PostMapping()
    public User addUser(@Valid @RequestBody User user) {

        return userService.addUser(user);
    }

    // обработка PUT-запроса на обновление данных пользователя
    @PutMapping()
    public User updateUser(@Valid @RequestBody User user) {

        return userService.updateUser(user);
    }

    // обработка GET-запроса на получение списка пользователей
    @GetMapping()
    public List<User> listUsers() {

        return userService.listUsers();
    }

    // обработка GET-запроса на получение пользователя по id
    @GetMapping("{id}")
    public User getUserById(@PathVariable Long id) {

        return userService.getUserById(id);
    }

    // обработка PUT-запроса на добавление друга
    @PutMapping("{id}/friends/{friendId}")
    public User addFriend(@RequestBody @PathVariable Long id, @PathVariable Long friendId) {

        return userService.addFriend(id, friendId);
    }

    // обработка DELETE-запроса на добавление друга
    @DeleteMapping("{id}/friends/{friendId}")
    public User deleteFriend(@RequestBody @PathVariable Long id, @PathVariable Long friendId) {

        return userService.deleteFriend(id, friendId);
    }

    // обработка GET-запроса на получение списка друзей
    @GetMapping("{id}/friends")
    public List<User> listUserFriends(@RequestBody @PathVariable Long id) {

        return userService.listUserFriends(id);
    }

    // обработка GET-запроса на получение списка общих друзей
    @GetMapping("{id}/friends/common/{otherId}")
    public List<User> listCommonFriends(@RequestBody @PathVariable Long id, @PathVariable Long otherId) {

        return userService.listCommonFriends(id, otherId);
    }

}






