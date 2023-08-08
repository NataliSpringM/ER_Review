package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserDoesNotExistException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;


// реализация сервиса для обработки запросов на создание / удаление / получение списков друзей пользователя
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceCommunity implements UserService {

    private final UserStorage userStorage;

    // добавление информации о пользователе в UserStorage
    @Override
    public User addUser(User user) {

        return userStorage.addUser(user);
    }

    // обновление информации о пользователе в UserStorage
    @Override
    public User updateUser(User user) {

        return userStorage.updateUser(user);
    }

    // получение списка пользователей из UserStorage
    @Override
    public List<User> listUsers() {

        return userStorage.listUsers();
    }

    // получение пользователя по идентификатору из UserStorage
    @Override
    public User getUserById(Long id) {

        return userStorage.getUserById(id);
    }

    // добавление друзей в UserStorage
    @Override
    public User addFriend(Long userId, Long friendId) {

        // проверка существования id пользователей
        checkIfUserIdExist(userId);
        checkIfUserIdExist(friendId);

        // добавляем пользователей в списки друзей друг друга
        // возвращение копий объектов с обновленным списком друзей
        User user = updateFriendList(userId, friendId, Command.ADD);
        User friend = updateFriendList(friendId, userId, Command.ADD);

        // обновление информации об объектах в хранилище
        userStorage.updateUserData(user);
        userStorage.updateUserData(friend);

        log.info("Сохранен друг {} с id {} для пользователя: {} c id {}",
                userStorage.getUserById(friendId), friendId, userStorage.getUserById(userId), userId);

        return userStorage.getUserById(userId);
    }

    // удаление из друзей в UserStorage
    @Override
    public User deleteFriend(Long userId, Long friendId) {

        // проверка существования id пользователей
        checkIfUserIdExist(userId);
        checkIfUserIdExist(friendId);

        // удаление пользователей из списков друзей друг друга
        // возвращение копий объектов с обновленным списком друзей
        User user = updateFriendList(userId, friendId, Command.DELETE);
        User friend = updateFriendList(friendId, userId, Command.DELETE);

        // обновление информации об объектах в хранилище
        userStorage.updateUserData(user);
        userStorage.updateUserData(friend);

        log.info("Удален друг {} с id {} для пользователя: {} c id {}",
                userStorage.getUserById(friendId), friendId, userStorage.getUserById(userId), userId);

        return userStorage.getUserById(userId);
    }

    // получение списка друзей пользователя из UserStorage
    @Override
    public List<User> listUserFriends(Long userId) {

        checkIfUserIdExist(userId);
        User user = userStorage.getUserById(userId);

        Set<Long> userFriends = returnEmptyCollectionIfNull(user.getFriends());

        log.info("У пользователя {} {} друзей в списке", user, userFriends.size());

        return convertIdSetToUserList(userFriends);
    }

    // получение списка общих друзей из UserStorage
    @Override
    public List<User> listCommonFriends(Long userId, Long otherId) {

        checkIfUserIdExist(userId);
        checkIfUserIdExist(otherId);

        // получаем пользователей по id
        User user = userStorage.getUserById(userId);
        User other = userStorage.getUserById(otherId);

        // проверка на null
        HashSet<Long> mutualFriends = new HashSet<>(returnEmptyCollectionIfNull(user.getFriends()));
        HashSet<Long> friendFriends = new HashSet<>(returnEmptyCollectionIfNull(other.getFriends()));

        // оставляем в коллекции только общих друзей
        mutualFriends.retainAll(friendFriends);

        log.info("Общих друзей в списке у пользователей {} и {} : {}", user, other, mutualFriends.size());

        // преобразуем набор id в список пользователей и возвращаем
        return convertIdSetToUserList(mutualFriends);
    }


    // обновление списка друзей пользователя в UserStorage
    private User updateFriendList(Long userId, Long friendId, Command command) {

        User user = userStorage.getUserById(userId);
        Set<Long> userFriends = returnEmptyCollectionIfNull(user.getFriends());

        // добавляем либо удаляем друга
        switch (command) {
            case ADD:
                userFriends.add(friendId);
                break;
            case DELETE:
                userFriends.remove(friendId);
                break;
        }

        // возвращаем копию объекта пользователя с обновленным списком друзей
        return user.toBuilder().friends(userFriends).build();
    }

    // возврат пустой коллекции в случае если обнаружено значение null
    private Set<Long> returnEmptyCollectionIfNull(Set<Long> set) {

        return Optional.ofNullable(set).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    // преобразование набора id в список пользователей
    private List<User> convertIdSetToUserList(Set<Long> set) {

        return set.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());

    }

    // проверка наличия id пользователя
    private void checkIfUserIdExist(Long userId) {

        if (!userStorage.getUsersData().containsKey(userId)) {
            throw new UserDoesNotExistException("Пользователь с id: " + userId + " не найден.", userId);
        }
    }

}



