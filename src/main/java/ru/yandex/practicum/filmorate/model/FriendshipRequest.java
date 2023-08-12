package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class FriendshipRequest {

    @NotNull
    Long initiatorId; // id пользователя, отправившего запрос на добавление в друзья
    @NotNull
    Long recipientId; // id пользователя, получившего запрос на добавление в друзья
    boolean isConfirmed; // статус подтверждения запроса

}
