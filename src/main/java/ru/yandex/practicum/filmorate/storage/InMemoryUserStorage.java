package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserDoesNotExistException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    // реализация хранения информации о пользователях в памяти

    private final HashMap<Long, User> users = new HashMap<>();

    private Long nextId = 1L;

    //добавление информации о пользователе
    @Override
    public User addUser(User user) {

        User newUser = user.toBuilder().id(nextId).build();
        nextId++;

        if (isFieldEmpty(user.getName())) { // устанавливаем логин в качестве имени в случае незаполненного поля
            newUser = newUser.toBuilder().name(user.getLogin()).build();
        }

        updateUserData(newUser); // сохранение информации о пользователе
        log.info("Сохранен пользователь: {}", user);

        return newUser;
    }

    // обновление информации о пользователе
    @Override
    public User updateUser(User user) {

        if (!users.containsKey(user.getId())) {
            throw new UserDoesNotExistException("Такого пользователя нет в списке.");
        }

        User newUser;

        if (isFieldEmpty(user.getName())) {
            newUser = user.toBuilder().name(user.getLogin()).build(); // устанавливаем логин в качестве имени в случае незаполненного поля
        } else {
            newUser = user;
        }

        updateUserData(newUser); // обновление информации о пользователе
        log.info("Обновлены данные пользователя {}", user);
        return newUser;

    }

    // получение списка пользователей
    @Override
    public List<User> listUsers() {

        List<User> listUsers = new ArrayList<>(users.values());

        log.info("Количество пользователей в списке: {}", listUsers.size());

        return listUsers;

    }

    @Override
    public User getUserById(Long id) { // получение пользователя по идентификатору

        return users.values().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new UserDoesNotExistException(String.format("Пользователь с id %d не найден", id)));
    }

    public void updateUserData(User user) { // сохранение новой или обновленной информации о пользователе
        users.put(user.getId(), user);
    }


    public Map<Long, User> getUsersData() { // получение данных о пользователях
        return users;
    }

    private boolean isFieldEmpty(String fieldValue) { // проверка является ли поле пустым
        return fieldValue == null || fieldValue.isBlank();
    }

}
