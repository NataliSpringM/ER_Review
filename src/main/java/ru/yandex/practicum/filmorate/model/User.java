package ru.yandex.practicum.filmorate.model;

import lombok.*;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Validated
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class User {

    //информация о пользователе - id, email, имя (необязательное поле), логин, дата рождения, друзья (список id)

    Long id; // id пользователя

    @Email
    @NotNull
    String email; // email пользователя


    @Nullable
    String name; // имя пользователя - необязательное поле

    @NotNull
    @NotBlank
    @NotEmpty
    @Pattern(regexp = "^\\S+$", message = "не должно содержать пробелы")
    String login; // login пользователя


    @PastOrPresent
    @NotNull
    LocalDate birthday; // дата рождения пользователя

    Set<Long> friends; // список id друзей пользователя

    Map<Long, FriendshipRequest> friendshipRequests; // id запроса на добавление в друзья и наличие подтверждения


}