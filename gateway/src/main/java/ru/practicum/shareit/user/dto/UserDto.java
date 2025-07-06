package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    @NotBlank(message = "Наименование должно быть указано")
    private String name;
    @Email(message = "Неправильный формат электронной почты")
    @NotBlank(message = "Необходимо указать адрес электронной почты")
    private String email;
}
