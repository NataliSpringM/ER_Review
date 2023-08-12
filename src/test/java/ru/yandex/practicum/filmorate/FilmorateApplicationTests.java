
package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;


import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import ru.yandex.practicum.filmorate.controllers.FilmController;
import ru.yandex.practicum.filmorate.controllers.UserController;
import ru.yandex.practicum.filmorate.exceptions.FilmDoesNotExistException;
import ru.yandex.practicum.filmorate.exceptions.UserDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import javax.validation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
public class FilmorateApplicationTests {

    @Autowired
    private UserController userController;
    @Autowired
    private FilmController filmController;
    @Autowired
    private InMemoryFilmStorage inMemoryFilmStorage;
    @Autowired
    private InMemoryUserStorage inMemoryUserStorage;
    private Map<Integer, Film> films;
    private Map<Long, User> users;
    private Validator validator;

    @BeforeEach
    void setUp() {

        users = inMemoryUserStorage.getUsersData();
        films = inMemoryFilmStorage.getFilmsData();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

    }


    //************************* Тестирование работы сервиса добавления в друзья *************************

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetUserByValidId() { //  получаем информацию о пользователе по существующему id

        // создаем пользователя

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();

        final Long userId = user.getId();

        userController.addUser(user);

        // получаем информацию о пользователе по id

        User userFromStorage = userController.getUserById(userId);

        // проверяем корректность информации о пользователе

        assertEquals(user.getId(), userFromStorage.getId(),
                "Получена неверная информация о пользователе, не совпадает id");
        assertEquals(user.getEmail(), userFromStorage.getEmail(),
                "Получена неверная информация о пользователе, не совпадает email");
        assertEquals(user.getLogin(), userFromStorage.getLogin(),
                "Получена неверная информация о пользователе, не совпадает логин");
        assertEquals(user.getName(), userFromStorage.getName(),
                "Получена неверная информация о пользователе, не совпадает имя");
        assertEquals(user.getBirthday(), userFromStorage.getBirthday(),
                "Получена неверная информация о пользователе, не совпадает дата рождения");
        assertEquals(user.getFriends(), userFromStorage.getFriends(),
                "Получена неверная информация о пользователе, не совпадает список друзей");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailGetUserByInvalidId() { //  получаем информацию о пользователе по несуществующему id

        final Long nonExistentUserId = -1L;
        // проверяем выброшенное исключение при попытке получить информацию о пользователе с несуществующим id

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> userController.getUserById(nonExistentUserId),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id -1 не найден", e.getMessage());

    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldAddFriendWithValidInfo() { //  добавление в друзья пользователя с существующими id

        // создаем пользователя и двух друзей

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();

        User friend = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        User friend2 = User.builder()
                .id(3L)
                .email("Olga@yandex.ru")
                .login("olga")
                .name("OLga")
                .birthday(LocalDate.of(2005, 10, 12))
                .build();

        final Long userId = user.getId();
        final Long friendId = friend.getId();
        final Long friend2Id = friend2.getId();

        userController.addUser(user);
        userController.addUser(friend);
        userController.addUser(friend2);

        // добавляем друзей

        userController.addFriend(userId, friendId);
        userController.addFriend(userId, friend2Id);

        // получаем списки друзей пользователя и друга, информацию о первом в списке пользователе

        List<User> friendsOfUser = users.get(userId).getFriends().stream()
                .map(inMemoryUserStorage::getUserById).collect(Collectors.toList());

        List<User> friendsOfFriend = users.get(friendId).getFriends().stream()
                .map(inMemoryUserStorage::getUserById).collect(Collectors.toList());

        User friendFromList = friendsOfUser.get(0);
        User userFromList = friendsOfFriend.get(0);

        // проверяем корректность сохраненной в списках друзей информации

        assertEquals(users.get(userId).getFriends().size(), 2,
                "Пользователи с валидными данными не добавлены в друзья");
        assertEquals(users.get(friendId).getFriends().size(), 1,
                "Пользователи с валидными данными не добавлены в друзья друг к другу");
        assertEquals(users.get(friend2Id).getFriends().size(), 1,
                "Пользователи с валидными данными не добавлены в друзья друг к другу");
        assertEquals(friend.getId(), friendFromList.getId(),
                "Пользователь не попал в список друзей после добавления, не совпадает id");
        assertEquals(user.getId(), userFromList.getId(),
                "Друг не попал в список друзей после добавления, не совпадает id");
        assertEquals(friend.getEmail(), friendFromList.getEmail(),
                "Пользователь не попал в список друзей после добавления, не совпадает email");
        assertEquals(user.getEmail(), userFromList.getEmail(),
                "Друг не попал в список друзей после добавления, не совпадает email");
        assertEquals(friend.getLogin(), friendFromList.getLogin(),
                "Пользователь не попал в список друзей после добавления, не совпадает логин");
        assertEquals(user.getLogin(), userFromList.getLogin(),
                "Друг не попал в список друзей после добавления, не совпадает логин");
        assertEquals(friend.getName(), friendFromList.getName(),
                "Пользователь не попал в список друзей после добавления, не совпадает имя");
        assertEquals(user.getName(), userFromList.getName(),
                "Друг не попал в список друзей после добавления, не совпадает имя");
        assertEquals(friend.getBirthday(), friendFromList.getBirthday(),
                "Пользователь не попал в список друзей после добавления, не совпадает дата рождения");
        assertEquals(user.getBirthday(), userFromList.getBirthday(),
                "Друг не попал в список друзей после добавления, не совпадает дата рождения");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailAddFriendDoubleWithValidInfo() { //
        // добавление в друзья пользователя с существующими id дважды
        // создаем пользователей

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();


        User friend = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        final Long userId = user.getId();
        final Long friendId = friend.getId();

        userController.addUser(user);
        userController.addUser(friend);

        //добавляем друга

        userController.addUser(friend); // добавляем друга повторно

        userController.addFriend(userId, friendId);

        // проверяем наличие одного пользователя в списке друзей и отсутствие дубля

        assertEquals(users.get(userId).getFriends().size(), 1,
                "Пользователь с валидными данными добавлен в друзья дважды");
        assertEquals(users.get(friendId).getFriends().size(), 1,
                "Пользователь с валидными данными добавлен в друзья дважды");

    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailAddFriendWithFriendNonExistentId() {
        //  добавление в друзья пользователя с несуществующим id

        // создаем пользователей

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();


        final Long userId = user.getId();
        final Long friendNonExistentId = 9999L;

        userController.addUser(user);

        // проверяем выброшенное исключение при попытке добавить друга с несуществующим id

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> userController.addFriend(userId, friendNonExistentId),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id: 9999 не найден.", e.getMessage());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailAddFriendByUserWithNonExistentId() {
        //  добавление в друзья пользователю с несуществующим id

        // создаем друга

        User friend = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();


        final Long userNonExistentId = 9999L;
        final Long friendId = friend.getId();

        userController.addUser(friend);

        // проверяем выброшенное исключение при попытке создать друга пользователю с несуществующим id

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> userController.addFriend(userNonExistentId, friendId),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id: 9999 не найден.", e.getMessage());


    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldDeleteFriendWithValidId() { //  удаление из друзей пользователя с валидными данными

        // создаем пользователей

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();


        User friend = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        final Long userId = user.getId();
        final Long friendId = friend.getId();

        userController.addUser(user);
        userController.addUser(friend);

        //добавляем друга
        userController.addFriend(userId, friendId);

        List<User> friendsOfUser = users.get(userId).getFriends().stream()
                .map(inMemoryUserStorage::getUserById).collect(Collectors.toList());

        List<User> friendsOfFriend = users.get(friendId).getFriends().stream()
                .map(inMemoryUserStorage::getUserById).collect(Collectors.toList());

        User friendFromList = friendsOfUser.get(0);
        User userFromList = friendsOfFriend.get(0);

        // проверяем наличие пользователя в списке друзей

        assertEquals(users.get(userId).getFriends().size(), 1,
                "Пользователь с валидными данными не добавлен в друзья");
        assertEquals(users.get(friendId).getFriends().size(), 1,
                "Пользователь с валидными данными не добавлен в друзья взаимно");
        assertEquals(friend.getId(), friendFromList.getId(),
                "Пользователь не попал в список друзей после добавления, не совпадает id");
        assertEquals(user.getId(), userFromList.getId(),
                "Друг не попал в список друзей после добавления, не совпадает id");

        // удаляем друга

        userController.deleteFriend(userId, friendId);
        assertEquals(users.get(userId).getFriends().size(), 0,
                "Пользователь с валидными данными не удален из друзей");
        assertEquals(users.get(friendId).getFriends().size(), 0,
                "Пользователь с валидными данными не удален из друзей");
        assertTrue(users.get(userId).getFriends().isEmpty(), "Cписок друзей не пуст после удаления");
        assertTrue(users.get(friendId).getFriends().isEmpty(), "Cписок друзей не пуст после удаления");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailDeleteFriendWithNonExistentId() { //  удаление из друзей пользователя с существующими id

        // создаем пользователей

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();


        User friend = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        final Long userId = user.getId();
        final Long friendId = friend.getId();
        final Long nonExistentFriendId = -1L;

        userController.addUser(user);
        userController.addUser(friend);

        //добавляем друга с существующим id

        userController.addFriend(userId, friendId);

        // удаляем друга с несуществующим id, проверяем выброшенное исключение

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> userController.deleteFriend(userId, nonExistentFriendId),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id: -1 не найден.", e.getMessage());

        // проверяем отсутствие изменений в списке друзей

        assertEquals(users.get(userId).getFriends().size(), 1,
                "Изменился размер списка друзей после некорректного удаления");
        assertEquals(users.get(friendId).getFriends().size(), 1,
                "Изменился размер списка друзей после некорректного удаления");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailDeleteFriendByUserWithNonExistentId() {
        //  удаление из друзей пользователя с существующими id

        // создаем пользователей

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();


        User friend = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        final Long userId = user.getId();
        final Long friendId = friend.getId();
        final Long nonExistentUserId = -1L;

        userController.addUser(user);
        userController.addUser(friend);

        //добавляем друга с существующим id

        userController.addFriend(userId, friendId);

        // удаляем друга с несуществующим айди, проверяем выброшенное исключение

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> userController.deleteFriend(nonExistentUserId, friendId),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id: -1 не найден.", e.getMessage());

        // проверяем отсутствие изменений в списке друзей

        assertEquals(users.get(userId).getFriends().size(), 1,
                "Изменился размер списка друзей после некорректного удаления");
        assertEquals(users.get(friendId).getFriends().size(), 1,
                "Изменился размер списка друзей после некорректного удаления");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetNotEmptyFriendList() { // получение непустого списка друзей у существующего пользователя

        // создаем пользователя и двух друзей

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();

        User friend = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        User friend2 = User.builder()
                .id(3L)
                .email("Olga@yandex.ru")
                .login("olga")
                .name("OLga")
                .birthday(LocalDate.of(2005, 10, 12))
                .build();

        final Long userId = user.getId();
        final Long friend1Id = friend.getId();
        final Long friend2Id = friend2.getId();

        userController.addUser(user);
        userController.addUser(friend);
        userController.addUser(friend2);

        // добавляем друзей

        userController.addFriend(userId, friend1Id);
        userController.addFriend(userId, friend2Id);

        // получаем список друзей пользователя и друга, информацию о втором в списке пользователе

        List<User> friendsOfUser = userController.listUserFriends(userId);

        User friendFromList = friendsOfUser.get(1);

        // проверяем корректность полученного списка друзей

        assertEquals(friendsOfUser.size(), 2,
                "Получен cписок друзей неверного размера");

        assertEquals(friend2.getId(), friendFromList.getId(),
                "Неверная информация о втором в списке друге, не совпадает id");
        assertEquals(friend2.getEmail(), friendFromList.getEmail(),
                "Неверная информация о втором в списке друге, не совпадает email");
        assertEquals(friend2.getLogin(), friendFromList.getLogin(),
                "Неверная информация о втором в списке друге, не совпадает логин");
        assertEquals(friend2.getName(), friendFromList.getName(),
                "Неверная информация о втором в списке друге, не совпадает имя");
        assertEquals(friend2.getBirthday(), friendFromList.getBirthday(),
                "Неверная информация о втором в списке друге, не совпадает дата рождения");
        assertEquals(userController.listUserFriends(friend2Id).size(), 1,
                "Неверная информация о втором в списке друге, неверный размер списка друзей");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailGetFriendListByInvalidId() { // получение списка друзей у несуществующего пользователя

        // проверяем выброшенное исключение при попытке создать друга пользователю с несуществующим id

        final Long userNonExistentId = -1L;

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> userController.listUserFriends(userNonExistentId),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id: -1 не найден.", e.getMessage());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetEmptyFriendList() { // получение пустого списка друзей у существующего пользователя

        // создаем пользователя

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();

        final Long userId = user.getId();

        userController.addUser(user);

        // получаем список друзей пользователя

        List<User> friendsOfUser = userController.listUserFriends(userId);

        // проверяем корректность полученного списка друзей

        assertEquals(friendsOfUser.size(), 0,
                "Получен cписок друзей неверного размера");
        assertTrue(friendsOfUser.isEmpty());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetNotEmptyListMutualFriendsWithValidInfo() { //  получение непустого списка общих друзей

        // создаем пользователя и трех его друзей

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();

        User friend = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        User friend2 = User.builder()
                .id(3L)
                .email("Olga@yandex.ru")
                .login("olga")
                .name("OLga")
                .birthday(LocalDate.of(2005, 10, 12))
                .build();

        User friend3 = User.builder()
                .id(4L)
                .email("Ivan@yandex.ru")
                .login("ivan")
                .name("Ivan")
                .birthday(LocalDate.of(1998, 5, 2))
                .build();


        final Long userId = user.getId();
        final Long friend1Id = friend.getId();
        final Long friend2Id = friend2.getId();
        final Long friend3Id = friend3.getId();

        userController.addUser(user);
        userController.addUser(friend);
        userController.addUser(friend2);
        userController.addUser(friend3);

        // добавляем друзей с id 2,3,4 пользователю

        userController.addFriend(userId, friend1Id);
        userController.addFriend(userId, friend2Id);
        userController.addFriend(userId, friend3Id);

        // добавляем друзей c id 3,4 другу с id 2

        userController.addFriend(friend1Id, friend2Id);
        userController.addFriend(friend1Id, friend3Id);

        // получаем список общих друзей у пользователя и друга с id 2

        List<User> mutualFriends = userController.listCommonFriends(userId, friend1Id);
        List<User> mutualFriends2 = userController.listCommonFriends(friend1Id, userId);

        // получаем список общих друзей у друга с id 4 и друга с id 2

        List<User> mutualFriends3 = userController.listCommonFriends(friend3Id, friend1Id);

        // проверяем корректность списков общих друзей

        assertEquals(mutualFriends.size(), 2,
                "Неверный размер списка общих друзей у пользователя");
        assertEquals(mutualFriends2.size(), 2,
                "Неверный размер списка общих друзей у друга пользователя");
        assertEquals(mutualFriends3.size(), 1,
                "Неверный размер списка общих друзей у друзей пользователя");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetEmptyMutualFriendWithValidInfo() { //  получение пустого списка общих друзей

        // создаем двух пользователей

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        final Long userId = user.getId();
        final Long user2Id = user2.getId();

        userController.addUser(user);
        userController.addUser(user2);

        // получаем список общих друзей пользователей

        List<User> mutualFriends = userController.listCommonFriends(userId, user2Id);

        // проверяем корректность списков общих друзей

        assertEquals(mutualFriends.size(), 0,
                "Неверный размер списка общих друзей");
        assertTrue(mutualFriends.isEmpty());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailGetMutualFriendListByInvalidId() {
        // получение списка общих друзей у несуществующего пользователя

        User user2 = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        // проверяем выброшенное исключение при попытке получить список общих друзей

        final Long userNonExistentId = -1L;
        final Long user2Id = user2.getId();

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> userController.listCommonFriends(userNonExistentId, user2Id),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id: -1 не найден.", e.getMessage());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailGetMutualFriendListByInvalidIdOfOtherUser() {
        // получение списка общих друзей c несуществующим пользователем

        User user = User.builder()
                .id(1L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        userController.addUser(user);

        // проверяем выброшенное исключение при попытке получить список общих друзей

        final Long userNonExistentId = -1L;
        final Long userId = user.getId();

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> userController.listCommonFriends(userId, userNonExistentId),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id: -1 не найден.", e.getMessage());

    }

    //************************* Тестирование работы сервиса по определению рейтинга фильмов ***************


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetFilmByValidId() { //  получаем информацию о фильме по существующему id

        // создаем фильм

        Film film = Film.builder()
                .id(1)
                .name("All hate Cris")
                .description("Good comedy")
                .releaseDate(LocalDate.of(2000, 10, 10))
                .duration(90)
                .likes(100L)
                .build();

        final Integer filmId = film.getId();

        filmController.addFilm(film);

        filmController.getFilmById(filmId);

        // получаем информацию о фильме по id

        Film filmFromStorage = filmController.getFilmById(filmId);

        // проверяем корректность информации о фильме

        assertEquals(film.getId(), filmFromStorage.getId(),
                "Получена неверная информация о фильме, не совпадает id");
        assertEquals(film.getName(), filmFromStorage.getName(),
                "Получена неверная информация о фильме,, не совпадает название фильма");
        assertEquals(film.getDescription(), filmFromStorage.getDescription(),
                "Получена неверная информация о фильме, не совпадает описание фильма");
        assertEquals(film.getReleaseDate(), filmFromStorage.getReleaseDate(),
                "Получена неверная информация о фильме, не совпадает дата выхода фильма");
        assertEquals(film.getDuration(), filmFromStorage.getDuration(),
                "Получена неверная информация о фильме, не совпадает продолжительность фильма");
        assertEquals(film.getLikes(), filmFromStorage.getLikes(),
                "Получена неверная информация о фильме, не совпадает количество лайков");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailGetFilmByInvalidId() { //  получаем информацию о фильме по несуществующему id

        // проверяем выброшенное исключение при попытке получить информацию о фильме с несуществующим id

        final Integer nonExistentFilmId = -1;

        FilmDoesNotExistException e = assertThrows(
                FilmDoesNotExistException.class,
                () -> filmController.getFilmById(nonExistentFilmId),
                "Не выброшено исключение FilmDoesNotExistException.");
        assertEquals("Фильм с id -1 не найден", e.getMessage());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldAddLikeWithValidId() { //  существующие пользователи ставит лайк фильму с существующим id
        // создаем двух пользователей и фильм

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();


        Film film = Film.builder()
                .id(1)
                .name("All hate Cris")
                .description("Good comedy")
                .releaseDate(LocalDate.of(2000, 10, 10))
                .duration(90)
                .likes(0L)
                .build();

        final Long userId = user.getId();
        final Long user2Id = user2.getId();
        final Integer filmId = film.getId();

        userController.addUser(user);
        userController.addUser(user2);
        filmController.addFilm(film);

        // ставим лайки

        filmController.addLike(filmId, userId);
        filmController.addLike(filmId, user2Id);

        // проверяем количество лайков

        assertEquals(films.get(filmId).getLikes(), 2,
                "Количество лайков не увеличилось");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailAddLikeWithSameUserId() { //  пользователь повторно ставит лайк фильму

        // создаем пользователя и фильм

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();

        Film film = Film.builder()
                .id(1)
                .name("All hate Cris")
                .description("Good comedy")
                .releaseDate(LocalDate.of(2000, 10, 10))
                .duration(90)
                .likes(0L)
                .build();

        final Long userId = user.getId();
        final Integer filmId = film.getId();

        userController.addUser(user);
        filmController.addFilm(film);

        // ставим лайк

        filmController.addLike(filmId, userId);

        // проверяем выброшенное исключение при попытке повторно поставить лайк

        RuntimeException e = assertThrows(
                RuntimeException.class,
                () -> filmController.addLike(filmId, userId),
                "Не выброшено исключение при попытке повторно поставить лайк с одного id.");
        assertEquals("Вы уже ставили лайк этому фильму", e.getMessage());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailAddLikeByUserWithInvalidId() { // ставим лайк фильму от пользователя с несуществующим id

        // создаем фильм

        Film film = Film.builder()
                .id(1)
                .name("All hate Cris")
                .description("Good comedy")
                .releaseDate(LocalDate.of(2000, 10, 10))
                .duration(90)
                .likes(100L)
                .build();


        final Integer filmId = film.getId();
        final Long nonExistentUserId = -1L;

        filmController.addFilm(film);

        // проверяем выброшенное исключение при попытке поставить лайк от пользователя с несуществующим id

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> filmController.addLike(filmId, nonExistentUserId),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id: -1 не найден.", e.getMessage());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailAddLikeFilmWithInvalidId() { // ставим лайк фильму с несуществующим id

        // создаем пользователя

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();


        final Integer nonExistentFilmId = -1;
        final Long userId = user.getId();

        userController.addUser(user);

        // проверяем выброшенное исключение при попытке поставить лайк фильму с несуществующим id

        FilmDoesNotExistException e = assertThrows(
                FilmDoesNotExistException.class,
                () -> filmController.addLike(nonExistentFilmId, userId),
                "Не выброшено исключение FilmDoesNotExistException.");
        assertEquals("Фильм c id: -1 не найден.", e.getMessage());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldDeleteLikeWithValidId() { //существующие пользователи удаляют лайки у фильма с существующим id
        // создаем двух пользователей и фильм

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();


        Film film = Film.builder()
                .id(1)
                .name("All hate Cris")
                .description("Good comedy")
                .releaseDate(LocalDate.of(2000, 10, 10))
                .duration(90)
                .likes(0L)
                .build();

        final Long userId = user.getId();
        final Long user2Id = user2.getId();
        final Integer filmId = film.getId();

        userController.addUser(user);
        userController.addUser(user2);
        filmController.addFilm(film);

        // ставим лайки

        filmController.addLike(filmId, userId);
        filmController.addLike(filmId, user2Id);

        // проверяем количество лайков

        assertEquals(films.get(filmId).getLikes(), 2,
                "Количество лайков не увеличилось");

        // удаляем лайк

        filmController.deleteLike(filmId, userId);

        // проверяем количество лайков

        assertEquals(films.get(filmId).getLikes(), 1,
                "Количество лайков не уменьшилось");

        // удаляем последний лайк

        filmController.deleteLike(filmId, user2Id);

        // проверяем количество лайков

        assertEquals(films.get(filmId).getLikes(), 0,
                "Удалены не все лайки");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailDeleteLikeByUserWithInvalidId() { // удаляем лайк от пользователя с несуществующим id

        // создаем фильм

        Film film = Film.builder()
                .id(1)
                .name("All hate Cris")
                .description("Good comedy")
                .releaseDate(LocalDate.of(2000, 10, 10))
                .duration(90)
                .likes(0L)
                .build();


        final Integer filmId = film.getId();
        final Long nonExistentUserId = -1L;

        filmController.addFilm(film);

        // проверяем выброшенное исключение при попытке удалить лайк от пользователя с несуществующим id

        UserDoesNotExistException e = assertThrows(
                UserDoesNotExistException.class,
                () -> filmController.addLike(filmId, nonExistentUserId),
                "Не выброшено исключение UserDoesNotExistException.");
        assertEquals("Пользователь с id: -1 не найден.", e.getMessage());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldFailDeleteLikeFilmWithInvalidId() { // удаляем лайк у фильма с несуществующим id

        // создаем пользователя

        User user = User.builder()
                .id(1L)
                .email("Alex@yandex.ru")
                .login("alex")
                .name("Alexandr Ivanov")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();


        final Integer nonExistentFilmId = -1;
        final Long userId = user.getId();

        userController.addUser(user);

        // проверяем выброшенное исключение при попытке удалить лайк у фильма с несуществующим id

        FilmDoesNotExistException e = assertThrows(
                FilmDoesNotExistException.class,
                () -> filmController.addLike(nonExistentFilmId, userId),
                "Не выброшено исключение FilmDoesNotExistException.");
        assertEquals("Фильм c id: -1 не найден.", e.getMessage());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    // набор пользователей  и фильмовс валидными данными
    public void shouldGetMostPopularFilms() {
        // получение списка наиболее популярных фильмов со значением параметра count по умолчанию

        // создаем 7 пользователей
        User user;
        for (long i = 1L; i <= 7L; i++) {
            user = User.builder()
                    .id(i)
                    .email("Alex@yandex.ru")
                    .login("alex")
                    .name("Alexandr Ivanov")
                    .birthday(LocalDate.of(2000, 10, 10))
                    .build();

            userController.addUser(user);
        }

        // создаем 14 фильмов
        Film film;
        for (int i = 1; i <= 14; i++) {
            film = Film.builder()
                    .id(i)
                    .name("All hate Cris " + i)
                    .description("Good comedy")
                    .releaseDate(LocalDate.of(2000, 10, 10))
                    .duration(90)
                    .likes(0L)
                    .build();
            filmController.addFilm(film);
        }

        // ставим лайки от каждого пользователя каждому фильму

        for (Long i = 1L; i <= 7L; i++) {
            for (int j = 1; j <= 14; j++) {
                filmController.addLike(j, i);
            }
        }

        // создаем новый фильм, добавляем лайк

        User additionalUser = User.builder()
                .id(8L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        userController.addUser(additionalUser);
        filmController.addLike(14, additionalUser.getId());

        filmController.deleteLike(7, 1L);
        filmController.deleteLike(7, 2L);

        // получаем список наиболее популярных фильмов

        List<Film> mostPopularFilms = filmController.listMostPopularFilms(null);

        // проверяем корректность списка самых популярных фильмов

        assertEquals(mostPopularFilms.size(), 10,
                "Неверное количество фильмов в списке");
        assertEquals(mostPopularFilms.get(0).getId(), 14,
                "Неверный id у самого популярного фильма");
        assertEquals(mostPopularFilms.get(0).getLikes(), 8,
                "Неверное количество лайков у самого популярного фильма");
        assertEquals(mostPopularFilms.get(9).getLikes(), 7,
                "Неверное количество лайков у самого непопулярного фильма");


    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    // набор пользователей  и фильмовс валидными данными
    public void shouldGetMostPopularFilmsCountParam10() { // получение списка 10 наиболее популярных фильмов

        // создаем 7 пользователей
        User user;
        for (long i = 1L; i <= 7L; i++) {
            user = User.builder()
                    .id(i)
                    .email("Alex@yandex.ru")
                    .login("alex")
                    .name("Alexandr Ivanov")
                    .birthday(LocalDate.of(2000, 10, 10))
                    .build();

            userController.addUser(user);
        }

        // создаем 14 фильмов
        Film film;
        for (int i = 1; i <= 14; i++) {
            film = Film.builder()
                    .id(i)
                    .name("All hate Cris " + i)
                    .description("Good comedy")
                    .releaseDate(LocalDate.of(2000, 10, 10))
                    .duration(90)
                    .likes(0L)
                    .build();
            filmController.addFilm(film);
        }

        // ставим лайки от каждого пользователя каждому фильму

        for (Long i = 1L; i <= 7L; i++) {
            for (int j = 1; j <= 14; j++) {
                filmController.addLike(j, i);
            }
        }

        // создаем новый фильм, добавляем лайк

        User additionalUser = User.builder()
                .id(8L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        userController.addUser(additionalUser);
        filmController.addLike(14, additionalUser.getId());

        // получаем список наиболее популярных фильмов

        List<Film> mostPopularFilms = filmController.listMostPopularFilms(10);

        // проверяем корректность списка самых популярных фильмов

        assertEquals(mostPopularFilms.size(), 10,
                "Неверное количество фильмов в списке");
        assertEquals(mostPopularFilms.get(0).getId(), 14,
                "Неверный id у самого популярного фильма");
        assertEquals(mostPopularFilms.get(0).getLikes(), 8,
                "Неверное количество лайков у самого популярного фильма");
        assertEquals(mostPopularFilms.get(9).getLikes(), 7,
                "Неверное количество лайков у самого непопулярного фильма");


    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    // набор пользователей  и фильмовс валидными данными
    public void shouldGetMostPopularFilmsWithNullLikes() { // получение списка 10 наиболее популярных фильмов

        // создаем 7 пользователей
        User user;
        for (long i = 1; i <= 7; i++) {
            user = User.builder()
                    .id(i)
                    .email("Alex@yandex.ru")
                    .login("alex")
                    .name("Alexandr Ivanov")
                    .birthday(LocalDate.of(2000, 10, 10))
                    .build();

            userController.addUser(user);
        }

        // создаем 14 фильмов
        Film film;
        for (int i = 1; i <= 14; i++) {
            film = Film.builder()
                    .id(i)
                    .name("All hate Cris " + i)
                    .description("Good comedy")
                    .releaseDate(LocalDate.of(2000, 10, 10))
                    .duration(90)
                    .likes(0L)
                    .build();
            filmController.addFilm(film);
        }

        // ставим лайки от каждого пользователя пяти фильмам, рейтинг остальных - нулевой

        for (Long i = 1L; i <= 7L; i++) {
            for (int j = 1; j <= 5; j++) {
                filmController.addLike(j, i);
            }
        }

        // создаем новый фильм, добавляем лайк одному из фильмов

        User additionalUser = User.builder()
                .id(8L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();

        userController.addUser(additionalUser);
        filmController.addLike(5, additionalUser.getId());

        // получаем список наиболее популярных фильмов

        List<Film> mostPopularFilms = filmController.listMostPopularFilms(10);

        // проверяем корректность списка самых популярных фильмов

        assertEquals(mostPopularFilms.size(), 10,
                "Неверное количество фильмов в списке");
        assertEquals(mostPopularFilms.get(0).getId(), 5,
                "Неверный id у самого популярного фильма");
        assertEquals(mostPopularFilms.get(0).getLikes(), 8,
                "Неверное количество лайков у самого популярного фильма");
        assertEquals(mostPopularFilms.get(9).getLikes(), 0,
                "Неверное количество лайков у самого непопулярного фильма");

    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    // набор пользователей  и фильмовс валидными данными
    public void shouldGetMostPopularFilmsCountParam3() { // получение списка 3 наиболее популярных фильмов

        // создаем 7 пользователей
        User user;
        for (long i = 1; i <= 7; i++) {
            user = User.builder()
                    .id(i)
                    .email("Alex@yandex.ru")
                    .login("alex")
                    .name("Alexandr Ivanov")
                    .birthday(LocalDate.of(2000, 10, 10))
                    .build();

            userController.addUser(user);
        }

        // создаем 14 фильмов
        Film film;
        for (int i = 1; i <= 14; i++) {
            film = Film.builder()
                    .id(i)
                    .name("All hate Cris " + i)
                    .description("Good comedy")
                    .releaseDate(LocalDate.of(2000, 10, 10))
                    .duration(90)
                    .likes(0L)
                    .build();
            filmController.addFilm(film);
        }

        // ставим лайки от каждого пользователя каждому фильму

        for (Long i = 1L; i <= 7L; i++) {
            for (int j = 1; j <= 14; j++) {
                filmController.addLike(j, i);
            }
        }

        // создаем новый фильм, добавляем лайк

        User additionalUser = User.builder()
                .id(8L)
                .email("Alla@yandex.ru")
                .login("alla")
                .name("Alla")
                .birthday(LocalDate.of(2000, 10, 11))
                .build();


        userController.addUser(additionalUser);
        filmController.addLike(14, additionalUser.getId());

        // получаем список наиболее популярных фильмов

        List<Film> mostPopularFilms = filmController.listMostPopularFilms(3);

        // проверяем корректность сохраненной в списках друзей информации

        assertEquals(mostPopularFilms.size(), 3,
                "Неверное количество фильмов в списке");
        assertEquals(mostPopularFilms.get(0).getId(), 14,
                "Неверный id у самого популярного фильма");
        assertEquals(mostPopularFilms.get(0).getLikes(), 8,
                "Неверное количество лайков у самого популярного фильма");
        assertEquals(mostPopularFilms.get(2).getLikes(), 7,
                "Неверное количество лайков у самого непопулярного фильма");

    }


    //************************* Тестирование работы с информацией о пользователях *************************

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldAddUserWithValidInfo() {

        //добавляем пользователя с валидными данными

        User user = new User(1L, "Alex@yandex.ru", "Alexandr Ivanov", "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());
        final Long id = user.getId();
        userController.addUser(user);

        // проверяем наличие пользователя в сохраненных данных

        assertEquals(users.get(id), user, "Валидные данные пользователя не сохранились");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldAddUserWithEmptyName() {

        //добавляем пользователя с пустым именем

        User user = new User(1L, "Alex@yandex.ru", null, "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());
        final Long id = user.getId();
        userController.addUser(user);

        // проверяем наличие пользователя в сохраненных данных и обновление имени значением логина

        assertEquals(users.get(id).getName(), users.get(id).getLogin(),
                "Имя не обновлено значением логина");

    }

    @ParameterizedTest
    @ArgumentsSource(UsersArgumentsProvider.class) // набор пользователей с навалидными данными
    public void shouldNotAddUser(User user) {

        //проверка выброшенного исключения при попытке сохранить пользователя с невалидными данными

        assertThrows(
                ConstraintViolationException.class,
                () -> userController.addUser(user),
                "Сохранены данные пользователя с невалидными данными.");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldPutUserWithValidInfo() {

        //сохраняем пользователя

        User user = new User(1L, "Alex@yandex.ru", "Alexandr Ivanov", "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());
        final Long id = user.getId();
        userController.addUser(user);

        //обновляем данные пользователя

        User userUpdated = new User(1L, "Egor@yandex.ru", "Egor Egorov", "egor",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());
        userController.updateUser(userUpdated);

        //проверяем корректность обновления данных

        assertEquals(users.get(id), userUpdated, "Данные пользователя не обновились");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldPutUserWithEmptyName() {

        //сохраняем пользователя с пустым именем

        User user = new User(1L, "Alex@yandex.ru", null, "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());
        final Long id = user.getId();
        userController.addUser(user);

        //обновляем данные пользователя

        User userUpdated = new User(1L, "Egor@yandex.ru", null, "egor",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());
        userController.updateUser(userUpdated);

        //проверяем корректность обновления данных и присвоения логина в качестве имени

        assertEquals(users.get(id).getName(), users.get(id).getLogin(),
                "Имя пользователя не обновлено значением логина");

    }

    @Test
    public void shouldFailPutUserWithInvalidLogin() {

        // обновляем данные пользователя  с несуществующим логином, проверяем выброшенное исключение

        User user = new User(10000L, "Egor@yandex.ru", "Egor Egorov", "egor",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        assertThrows(
                UserDoesNotExistException.class,
                () -> userController.updateUser(user),
                "Такого пользователя нет в списке.");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetUsersList() {

        // добавляем пользователей в список

        User user1 = new User(1L, "Alex@yandex.ru", null, "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());
        userController.addUser(user1);

        User user2 = new User(1L, "Egor@yandex.ru", null, "egor",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());
        userController.addUser(user2);

        //получаем пользователей из списка

        List<User> usersList = userController.listUsers();
        assertEquals(usersList.size(), 2, "Список пользователей неверного размера");

    }

    //************************* Тестирование работы с информацией о Фильмах *************************
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldAddFilmWithValidInfo() {

        //добавляем фильм с валидными данными

        Film film = new Film(1, "All hate Cris", " Good comedy",
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));
        final Integer id = film.getId();
        filmController.addFilm(film);

        // проверяем наличие пользователя в сохраненных данных

        assertEquals(films.get(id), film, "Валидные данные фильма не сохранились");

    }


    @ParameterizedTest
    @ArgumentsSource(FilmsArgumentsProvider.class) // набор фильмов с навалидными данными
    public void shouldNotAddFilmWithInvalidData(Film film) {

        //проверка выброшенного исключения при попытке сохранить пользователя с невалидными данными

        assertThrows(
                ConstraintViolationException.class,
                () -> filmController.addFilm(film),
                "Сохранены данные фильма с невалидными данными.");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldPutFilmWithValidInfo() {

        //сохраняем данные о фильме

        Film film = new Film(1, "All hate Cris", " Good comedy",
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));
        final Integer id = film.getId();
        filmController.addFilm(film);

        //обновляем данные фильма

        Film filmUpdated = new Film(1, "All like Tests", " Real comedy",
                LocalDate.of(2023, 7, 19), 12000, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));
        filmController.updateFilm(filmUpdated);

        //проверяем корректность обновления данных

        assertEquals(films.get(id), filmUpdated, "Данные фильма не обновились");

    }

    @Test
    public void shouldFailPutFilmWithInvalidLogin() {

        // обновляем данные фильма с несуществующим логином, проверяем выброшенное исключение

        Film film = new Film(10000, "All hate Cris", " Good comedy",
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));

        assertThrows(
                FilmDoesNotExistException.class,
                () -> filmController.updateFilm(film),
                "Такого фильма нет в списке.");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldGetFilmsList() {

        // добавляем фильмы в список

        Film film1 = new Film(1, "All hate Cris", " Good comedy",
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));
        filmController.addFilm(film1);

        Film film2 = new Film(1, "All like Tests", " Real comedy",
                LocalDate.of(2023, 7, 19), 12000, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));
        filmController.addFilm(film2);

        //получаем фильмы из списка

        List<Film> filmsList = filmController.listFilms();
        assertEquals(filmsList.size(), 2, "Список фильмов неверного размера");

    }

    //************************* Тесты на валидацию данных для фильмов *************************
    @Test
    public void shouldPassValidationFilmWithValidData() { //filmData is valid = should pass

        Film film = new Film(1, "All hate Cris", " Good comedy",
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Валидные данные фильма не прошли проверку");

    }

    @Test
    public void shouldFailValidationFilmWithEmptyName() { // filmName is empty

        Film film = new Film(1, "", " Good comedy",
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(),
                "Поле название фильма, содержащее только пробелы, прошло валидацию");
        assertEquals(violations.size(), 2,
                "Неверное количество ошибок при проверке пустого поля с названием фильма");

        for (ConstraintViolation<Film> violation : violations) {

            assertEquals(violation.getInvalidValue(), film.getName(),
                    "Неверно определено невалидное значение: поле с названием фильма");
        }
    }

    @Test
    public void shouldFailValidationFilmWithBlankName() { // fillName is blank

        Film film = new Film(1, " ", "Good comedy",
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Пустое поле с названием фильма прошло валидацию");
        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке пустого поля с названием фильма");

        for (ConstraintViolation<Film> violation : violations) {

            assertEquals(violation.getInvalidValue(), film.getName(),
                    "Неверно определено невалидное значение: поле с названием фильма");
        }
    }


    @Test
    public void shouldFailValidationFilmWithNullName() { // filmName is null

        Film film = new Film(1, null, " Good comedy",
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Поле с названием фильма со значением null прошло валидацию");

        assertEquals(violations.size(), 3,
                "Неверное количество ошибок при проверке поля с названием фильма cо значением null");


        for (ConstraintViolation<Film> violation : violations) {

            assertEquals(violation.getInvalidValue(), film.getName(),
                    "Неверное определено невалидное значение: поле с названием фильма");

        }
    }

    @Test
    public void shouldFailValidationFilmWithTooLongDescription() { // description is too long

        Film film = new Film(1, "All hate Cris",
                " Good comedy, but it's too long description. it's too long description"
                        + "it's too long description. it's too long description. it's too long description. "
                        + "it's too long description. it's too long description. it's too long description. "
                        + "it's too long description. it's too long description.",
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Поле с описанием фильма, содержащее более 200 символов,"
                + " прошло валидацию");

        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке пустого поля с названием фильма");

        for (ConstraintViolation<Film> violation : violations) {

            assertEquals(violation.getInvalidValue(), film.getDescription(),
                    "Неверно определено невалидное значение: поле с описанием фильма");
        }
    }

    @Test
    public void shouldFailValidationFilmWithNullDescription() { // description is null

        Film film = new Film(1, "All hate Cris", null,
                LocalDate.of(2002, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));


        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Поле с описанием фильма со значением null"
                + " не прошло валидацию");

        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке поля с описанием фильма cо значением null");

    }

    @Test
    public void shouldFailValidationFilmWithInvalidReleaseDate() { //releaseDate is not valid

        Film film = new Film(1, "All hate Cris", " Good comedy",
                LocalDate.of(1700, 2, 10), 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Пустое поле с невалидной датой выхода фильма прошло валидацию");
        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке поля с невалидной датой выхода фильма");

        for (ConstraintViolation<Film> violation : violations) {

            assertEquals(violation.getMessage(), "не должно быть ранее 28-12-1895",
                    "Не содержит сообщения об ошибке 'не должно быть ранее 28-12-1895'");

            assertEquals(violation.getInvalidValue(), film.getReleaseDate(),
                    "Неверно определено невалидное значение: поле с датой выхода фильма");
        }
    }

    @Test
    public void shouldFailValidationFilmWithNullReleaseDate() { //releaseDate is null

        Film film = new Film(1, "All hate Cris", "Good comedy",
                null, 40, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Поле с датой выхода фильма со значением null"
                + " не прошло валидацию");

        assertEquals(violations.size(), 2,
                "Неверное количество ошибок при проверке поля с датой выхода фильма cо значением null");

        for (ConstraintViolation<Film> violation : violations) {

            assertEquals(violation.getInvalidValue(), film.getReleaseDate(),
                    "Неверное определено невалидное значение: поле с датой выхода фильма");

        }
    }

    @Test
    public void shouldFailValidationFilmWithNegativeDurationValue() { // duration is negative

        Film film = new Film(1, "All hate Cris", " Good comedy",
                LocalDate.of(2002, 2, 10), (-900), 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(),
                "Поле с отрицательным значением продолжительности фильма прошло валидацию");

        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке поля "
                        + "с отрицательным значением продолжительности фильма");

        for (ConstraintViolation<Film> violation : violations) {

            assertEquals(violation.getInvalidValue(), film.getDuration(),
                    "Неверно определено невалидное значение: поле с продолжительностью фильма");
        }
    }

    @Test
    public void shouldFailValidationFilmWithNotPositiveDuration() { // duration is not positive

        Film film = new Film(1, "All hate Cris", " Good comedy",
                LocalDate.of(2002, 2, 10), 0, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(),
                "Поле с нулевым значением продолжительности фильма прошло валидацию");

        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке поля "
                        + "с нулевым значением продолжительности фильма");

        for (ConstraintViolation<Film> violation : violations) {

            assertEquals(violation.getInvalidValue(), film.getDuration(),
                    "Неверно определено невалидное значение: поле с продолжительностью фильма");
        }

    }

    @Test
    public void shouldFailValidationFilmWithNullDuration() { // duration is null

        Film film = new Film(1, "All hate Cris", null,
                LocalDate.of(2002, 2, 10), null, 0L,
                RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(),
                "Поле с продолжительностью фильма со значением null прошло валидацию");

        assertEquals(violations.size(), 2,
                "Неверное количество ошибок при проверке поля "
                        + "с продолжительностью фильма cо значением null");

        for (ConstraintViolation<Film> violation : violations) {

            assertEquals(violation.getInvalidValue(), film.getDuration(),
                    "Неверное определено невалидное значение: поле с продолжительностью фильма");

        }

    }

    //************************* Тесты на валидацию данных пользователя *************************
    @Test
    public void shouldPassValidationUserWithValidData() { // login is valid = should pass

        User user = new User(1L, "Alex@yandex.ru", "Alexandr Ivanov", "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Валидные данные пользователя не прошли проверку");

    }

    @Test
    public void shouldFailValidationUserWithEmptyLogin() { // login is empty

        User user = new User(1L, "Alex@yandex.ru", "Alexandr Ivanov", "",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пустое поле логин пользователя прошло валидацию");
        assertEquals(violations.size(), 3,
                "Неверное количество ошибок при проверке пустого поля логин пользователя");

        for (ConstraintViolation<User> violation : violations) {

            assertEquals(violation.getInvalidValue(), user.getLogin(),
                    "Неверно определено невалидное значение: поле логин пользователя");
        }
    }

    @Test
    public void shouldFailValidationUserWithBlankLogin() { // login is blank

        User user = new User(1L, "Alex@yandex.ru", "Alexandr Ivanov", " ",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(),
                "Поле логин пользователя, содержащее только пробелы, прошло валидацию");
        assertEquals(violations.size(), 2,
                "Неверное количество ошибок при проверке пустого поля логин пользователя");

        for (ConstraintViolation<User> violation : violations) {

            assertEquals(violation.getInvalidValue(), user.getLogin(),
                    "Неверно определено невалидное значение: поле логин пользователя");
        }
    }

    @Test
    public void shouldFailValidationUserWithNullLogin() { // login is null

        User user = new User(1L, "Alex@yandex.ru", "Alexandr Ivanov", null,
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Поле логин пользователя со значением null прошло валидацию");
        assertEquals(violations.size(), 3,
                "Неверное количество ошибок при проверке поля логин пользователя со значением null");

        for (ConstraintViolation<User> violation : violations) {

            assertEquals(violation.getInvalidValue(), user.getLogin(),
                    "Неверно определено невалидное значение: поле логин пользователя");
        }
    }

    @Test
    public void shouldFailValidationUserWithLoginContainsSpace() { // login contains space

        User user = new User(1L, "Alex@yandex.ru", "Alexandr Ivanov", "Alex Alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Поле логин пользователя со значением null прошло валидацию");
        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке поля логин пользователя со значением null");

        for (ConstraintViolation<User> violation : violations) {

            assertEquals(violation.getInvalidValue(), user.getLogin(),
                    "Неверно определено невалидное значение: поле логин пользователя");
        }
    }

    @Test
    public void shouldPassValidationUserWithEmptyName() { // userName is Empty = should pass

        User user = new User(1L, "Alex@yandex.ru", "", "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Пустое поля имя пользователя не прошло проверку");

    }

    @Test
    public void shouldPassValidationUserWithBlankName() { // userName is Blank = should pass

        User user = new User(1L, "Alex@yandex.ru", " ", "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Имя пользователя, содержащее только пробелы, не прошло проверку");

    }

    @Test
    public void shouldPassValidationUserWithNullName() { // userName is null = should pass

        User user = new User(1L, "Alex@yandex.ru", null, "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(),
                "Имя пользователя, содержащее значение null, не прошло проверку");

    }

    @Test
    public void shouldFailValidationUserWithInvalidEmail() { // invalid email

        User user = new User(1L, "Alex НДЕКС_? @ yandex.ru", "Alex Ivanov", "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Поле с невалидным значением email прошло валидацию");
        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке поля email с некорректным форматом");

        for (ConstraintViolation<User> violation : violations) {

            assertEquals(violation.getInvalidValue(), user.getEmail(),
                    "Неверно определено невалидное значение: поле email");
        }

    }

    @Test
    public void shouldFailValidationUserWithNullEmail() { //email is null

        User user = new User(1L, null, "Alex Ivanov", "alex",
                LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Поле email со значением null прошло валидацию");
        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке поля email со значением null");

        for (ConstraintViolation<User> violation : violations) {

            assertEquals(violation.getInvalidValue(), user.getEmail(),
                    "Неверно определено невалидное значение: поле email");
        }
    }

    @Test
    public void shouldFailValidationUserWithFutureBirthday() { //birthday is in future

        User user = new User(1L, "Alex@yandex.ru", "Alex Ivanov", "alex",
                LocalDate.of(2990, 10, 10), new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Поле дата рождения с датой в будущем времени прошло валидацию");
        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке поля дата рождения с датой в будущем времени");

        for (ConstraintViolation<User> violation : violations) {

            assertEquals(violation.getInvalidValue(), user.getBirthday(),
                    "Неверно определено невалидное значение: поле дата рождения");
        }
    }

    @Test
    public void shouldFailValidationUserWithNullBirthday() { // birthday is null

        User user = new User(1L, "Alex@yandex.ru", "Alex Ivanov", "alex",
                null, new HashSet<>(), new HashMap<>());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Поле дата рождения со значением null прошло валидацию");
        assertEquals(violations.size(), 1,
                "Неверное количество ошибок при проверке поля дата рождения со значением null");

        for (ConstraintViolation<User> violation : violations) {

            assertEquals(violation.getInvalidValue(), user.getBirthday(),
                    "Неверно определено невалидное значение: поле дата рождения");
        }
    }

    @Test
    void contextLoads() {
    }

    //************************* Набор невалидных данных пользователей *************************
    static class UsersArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(

                    //пустой логин

                    Arguments.of(new User(1L, "Alex@yandex.ru", "Alexandr Ivanov", "",
                            LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>())),
                    Arguments.of(new User(2L, "Alex@yandex.ru", "Alexandr Ivanov", null,
                            LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>())),
                    Arguments.of(new User(3L, "Alex@yandex.ru", "Alexandr Ivanov", " ",
                            LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>())),

                    //пустой или невалидный формат email

                    Arguments.of(new User(4L, null, "Alexandr Ivanov", "alex",
                            LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>())),
                    Arguments.of(new User(5L, "invalid## @ mail.ru",
                            "Alexandr Ivanov", "alex",
                            LocalDate.of(1990, 10, 10), new HashSet<>(), new HashMap<>())),

                    //пустая или невалидная дата рождения

                    Arguments.of(new User(6L, "Alex@yandex.ru", "Alexandr Ivanov", "alex",
                            null, new HashSet<>(), new HashMap<>())),
                    Arguments.of(new User(7L, "Alex@yandex.ru", "Alexandr Ivanov", "alex",
                            LocalDate.of(2100, 10, 10), new HashSet<>(), new HashMap<>())));
        }
    }

    //************************* Набор невалидных данных фильмов *************************
    static class FilmsArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(

                    // пустое название фильма

                    Arguments.of(new Film(1, "", " Good comedy",
                                    LocalDate.of(2002, 2, 10), 40, 0L,
                            RatingMPA.NC_17, Collections.singleton(Genre.COMEDY))),
                    Arguments.of(new Film(1, " ", "Good comedy",
                            LocalDate.of(2002, 2, 10), 40, 0L,
                            RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)))),
                    Arguments.of(new Film(1, null, " Good comedy",
                            LocalDate.of(2002, 2, 10), 40, 0L,
                                    RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)))),

                    // пустое или слишком длинное описание фильма

                    Arguments.of(new Film(1, "All hate Cris",
                            " Good comedy, but it's too long description. it's too long description"
                                    + "it's too long description. it's too long description."
                                    + " it's too long description. it's too long description. "
                                    + "it's too long description. it's too long description. "
                                    + "it's too long description. it's too long description.",
                            LocalDate.of(2002, 2, 10), 40, 0L,
                                    RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)))),
                    Arguments.of(new Film(1, "All hate Cris", null,
                            LocalDate.of(2002, 2, 10), 40, 0L,
                                    RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)))),

                    // пустая или невалидная дата выхода фильма

                    Arguments.of(new Film(1, "All hate Cris", " Good comedy",
                            LocalDate.of(1700, 2, 10), 40, 0L,
                                    RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)))),
                    Arguments.of(new Film(1, "All hate Cris", " Good comedy",
                            null, 40, 0L,
                                    RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)))),

                    // негативное или нулевое значение продолжительности фильма

                    Arguments.of(new Film(1, "All hate Cris", " Good comedy",
                            LocalDate.of(2002, 2, 10), 0, 0L,
                                    RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)))),
                    Arguments.of(new Film(1, "All hate Cris", " Good comedy",
                            LocalDate.of(2002, 2, 10), -900, 0L,
                            RatingMPA.NC_17, Collections.singleton((Genre.COMEDY)))));
        }
    }

}
