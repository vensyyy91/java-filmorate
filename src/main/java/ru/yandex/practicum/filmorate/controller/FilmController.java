package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService service;

    @GetMapping
    public List<Film> getAll() {
        log.info("Получен запрос GET /films");
        return service.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film get(@PathVariable int id) {
        log.info("Получен запрос GET /films/" + id);
        return service.getFilm(id);
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        log.info("Получен запрос POST /films");
        return service.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос PUT /films");
        return service.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос PUT /films/{}/like/{}", id, userId);
        service.like(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос DELETE /films/{}/like/{}", id, userId);
        service.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getTopLikes(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос GET /films/popular");
        return service.getTopLikes(count);
    }
}