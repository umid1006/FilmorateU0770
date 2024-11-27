package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(User.class); // Добавляем логгер


    @GetMapping
    public List<User> findAll() {
        return users;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        try {
            validateUser(user);
            int newId = generateId();
            user.setId(newId);
            users.add(user);
            log.info("Добавлен новый пользователь: {}", user);
            return user;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при создании пользователя: {}", e.getMessage());
            throw e;
        }
    }

    private int generateId() {
        // Логика для генерации уникального ID
        // (например, на основе максимального существующего ID + 1)
        int maxId = users.stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0);
        return maxId + 1;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        try {
            // 1. Проверка на наличие ID
            if (user.getId() == 0) {
                throw new ValidationException("ID пользователя должен быть указан");
            }
            // 2. Поиск пользователя по ID
            User existingUser = users.stream()
                    .filter(u -> u.getId() == user.getId())
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + user.getId() + " не найден"));
            // 3. Обновление полей пользователя (после того, как пользователь найден)
            if (user.getEmail() != null) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getLogin() != null) {
                existingUser.setLogin(user.getLogin());
            }
            if (user.getName() != null && !user.getName().isBlank()) { // Check if name is not empty
                existingUser.setName(user.getName());
            }
            if (user.getBirthday() != null) {
                existingUser.setBirthday(user.getBirthday());
            } else { // If name is empty or null
                // Either keep the existing name or set it to the login
                existingUser.setName(existingUser.getLogin()); // Or keep the existing name
            }
            log.info("Обновлен пользователь: {}", existingUser);
            return existingUser;
        } catch (ValidationException | ResourceNotFoundException e) {
            log.warn("Ошибка при обновлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @.");
        }
        if
        (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы.");

        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");

        }
    }
}