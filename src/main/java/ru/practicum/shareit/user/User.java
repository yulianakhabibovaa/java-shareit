package ru.practicum.shareit.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", schema = "public")
public class User {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Email(message = "Неправильный формат электронной почты")
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;
}
