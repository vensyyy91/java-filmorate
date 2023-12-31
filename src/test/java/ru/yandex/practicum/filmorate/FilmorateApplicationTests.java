package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.FriendsDao;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.LikesDao;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.FilmDbStorage;
import ru.yandex.practicum.filmorate.repository.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationTests {
	private final JdbcTemplate jdbcTemplate;
	private final FilmDbStorage filmStorage;
	private final UserDbStorage userStorage;
	private final GenreDao genreDao;
	private final MpaDao mpaDao;
	private final LikesDao likesDao;
	private final FriendsDao friendsDao;
	private static final Genre GENRE_COMEDY = new Genre(1, "Комедия");
	private static final Genre GENRE_DRAMA = new Genre(2, "Драма");
	private static final Genre GENRE_CARTOON = new Genre(3, "Мультфильм");
	private static final Genre GENRE_THRILLER = new Genre(4, "Триллер");
	private static final Genre GENRE_DOCUMENTARY = new Genre(5, "Документальный");
	private static final Genre GENRE_ACTION = new Genre(6, "Боевик");
	private static final Mpa MPA_G = new Mpa(1, "G");
	private static final Mpa MPA_PG = new Mpa(2, "PG");
	private static final Mpa MPA_PG_13 = new Mpa(3, "PG-13");
	private static final Mpa MPA_R = new Mpa(4, "R");
	private static final Mpa MPA_NC_17 = new Mpa(5, "NC-17");


	@BeforeEach
	public void init() {
		String sqlAddFilms = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES " +
				"('film1', 'first test film', '1990-09-10', 150, 2), " +
				"('film2', 'second test film', '2005-12-04', 120, 1), " +
				"('film3', 'third test film', '2008-10-01', 180, 4)";
		String sqlAddUsers = "INSERT INTO users (email, login, name, birthday) VALUES " +
				"('1@yandex.ru', 'user1', 'first', '1992-03-04'), " +
				"('2@yandex.ru', 'user2', 'second', '1994-10-14'), " +
				"('3@yandex.ru', 'user3', 'third', '1996-06-20')";
		String sqlAddGenres = "INSERT INTO film_genre (film_id, genre_id) " +
				"VALUES (1, 1), (2, 4), (3, 2), (3, 4), (3, 6)";
		String sqlAddLikes = "INSERT INTO likes (film_id, user_id) " +
				"VALUES (1, 2), (2, 1), (2, 2), (3, 1), (3, 2), (3, 3)";
		String sqlAddFriends = "INSERT INTO friends (user_id, friend_id) " +
				"VALUES (1, 2), (1, 3), (2, 3), (3, 1)";
		jdbcTemplate.update(sqlAddFilms);
		jdbcTemplate.update(sqlAddUsers);
		jdbcTemplate.update(sqlAddGenres);
		jdbcTemplate.update(sqlAddLikes);
		jdbcTemplate.update(sqlAddFriends);
	}

	@Test
	public void getAllFilms() {
		List<Film> films = filmStorage.getAll();

		assertThat(films).hasSize(3);
		assertThat(films.get(0)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG));
		assertThat(films.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G));
		assertThat(films.get(2)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R));
	}

	@Test
	public void getAllFilmsWhenEmpty() {
		jdbcTemplate.update("DELETE FROM film_genre");
		jdbcTemplate.update("DELETE FROM likes");
		jdbcTemplate.update("DELETE FROM films");
		List<Film> films = filmStorage.getAll();

		assertThat(films).hasSize(0);
	}

	@Test
	public void getFilmById() {
		Film film = filmStorage.getById(1);

		assertThat(film).isNotNull();
		assertThat(film).hasFieldOrPropertyWithValue("id", 1);
	}

	@Test
	public void getFilmByNonExistentId() {
		assertThatThrownBy(() -> filmStorage.getById(999)).hasMessage("Фильм с id=999 не найден.");
	}

	@Test
	public void addFilm() {
		Film film = new Film("film4", "fourth test film",
				LocalDate.of(2010, 8, 20), 140,
				Set.of(GENRE_COMEDY, GENRE_CARTOON), MPA_NC_17);
		Film filmReturned = filmStorage.save(film);
		Film filmFromDb = filmStorage.getById(4);

		assertThat(filmReturned).isNotNull();
		assertThat(filmReturned).hasFieldOrPropertyWithValue("id", 4);
		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
		assertThat(filmFromDb).isNotNull();
		assertThat(filmFromDb).isEqualTo(filmReturned);
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("id", 4);
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("name", "film4");
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
	}

	@Test
	public void addFilmWithDuplicateGenre() {
		Film film = new Film("film4", "fourth test film",
				LocalDate.of(2010, 8, 20), 140,
				new TreeSet<>(List.of(GENRE_COMEDY, GENRE_CARTOON, GENRE_COMEDY)), MPA_NC_17);
		Film filmReturned = filmStorage.save(film);
		Film filmFromDb = filmStorage.getById(4);

		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
	}

	@Test
	public void updateFilm() {
		Film updatedFilm = new Film(1, "updatedFilm1", "first test film updated",
				LocalDate.of(1990, 9, 10), 150,
				Set.of(GENRE_COMEDY, GENRE_CARTOON), MPA_PG_13);
		Film filmReturned = filmStorage.save(updatedFilm);
		Film filmFromDb = filmStorage.getById(1);

		assertThat(filmReturned).isNotNull();
		assertThat(filmReturned).hasFieldOrPropertyWithValue("id", 1);
		assertThat(filmReturned).hasFieldOrPropertyWithValue("name", "updatedFilm1");
		assertThat(filmReturned).hasFieldOrPropertyWithValue("mpa", MPA_PG_13);
		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
		assertThat(filmFromDb).isNotNull();
		assertThat(filmFromDb).isEqualTo(filmReturned);
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("id", 1);
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("name", "updatedFilm1");
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("mpa", MPA_PG_13);
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
	}

	@Test
	public void updateFilmWithFewerGenres() {
		Film updatedFilm = new Film(3, "updatedFilm3", "third test film",
				LocalDate.of(2008, 10, 1), 180,
				Set.of(GENRE_DRAMA, GENRE_THRILLER), MPA_R);
		Film filmReturned = filmStorage.save(updatedFilm);
		Film filmFromDb = filmStorage.getById(3);

		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_DRAMA, GENRE_THRILLER);
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_DRAMA, GENRE_THRILLER);
	}

	@Test
	public void updateFilmWithDuplicateGenre() {
		Film updatedFilm = new Film(1, "updatedFilm1", "first test film updated",
				LocalDate.of(1990, 9, 10), 150,
				new TreeSet<>(List.of(GENRE_COMEDY, GENRE_CARTOON, GENRE_CARTOON, GENRE_COMEDY)), MPA_PG_13);
		Film filmReturned = filmStorage.save(updatedFilm);
		Film filmFromDb = filmStorage.getById(1);

		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
	}

	@Test
	public void getAllUsers() {
		List<User> users = userStorage.getAll();

		assertThat(users).hasSize(3);
		assertThat(users.get(0)).isEqualTo(new User(1, "1@yandex.ru", "user1", "first",
				LocalDate.of(1992, 3, 4)));
		assertThat(users.get(1)).isEqualTo(new User(2, "2@yandex.ru", "user2", "second",
				LocalDate.of(1994, 10, 14)));
		assertThat(users.get(2)).isEqualTo(new User(3, "3@yandex.ru", "user3", "third",
				LocalDate.of(1996, 6, 20)));
	}

	@Test
	public void getAllUsersWhenEmpty() {
		jdbcTemplate.update("DELETE FROM friends");
		jdbcTemplate.update("DELETE FROM likes");
		jdbcTemplate.update("DELETE FROM users");
		List<User> users = userStorage.getAll();

		assertThat(users).hasSize(0);
	}

	@Test
	public void getUserById() {
		User user = userStorage.getById(1);

		assertThat(user).isNotNull();
		assertThat(user).hasFieldOrPropertyWithValue("id", 1);
	}

	@Test
	public void getUserByNonExistentId() {
		assertThatThrownBy(() -> userStorage.getById(999)).hasMessage("Пользователь с id=999 не найден.");
	}

	@Test
	public void addUser() {
		User user = new User("4@yandex.ru", "user4", "fourth",
				LocalDate.of(1998, 7, 16));
		User userReturned = userStorage.save(user);
		User userFromDb = userStorage.getById(4);

		assertThat(userReturned).isNotNull();
		assertThat(userReturned).hasFieldOrPropertyWithValue("id", 4);
		assertThat(userFromDb).isNotNull();
		assertThat(userFromDb).isEqualTo(userReturned);
		assertThat(userFromDb).hasFieldOrPropertyWithValue("id", 4);
		assertThat(userFromDb).hasFieldOrPropertyWithValue("email", "4@yandex.ru");
	}

	@Test
	public void updateUser() {
		User user = new User("4@yandex.ru", "user4", "fourth",
				LocalDate.of(1998, 7, 16));
		userStorage.save(user);
		User updatedUser = new User(4, "4@google.com", "user4", "the fourth",
				LocalDate.of(1998, 7, 16));
		User userReturned = userStorage.save(updatedUser);
		User userFromDb = userStorage.getById(4);

		assertThat(userReturned).isNotNull();
		assertThat(userReturned).hasFieldOrPropertyWithValue("id", 4);
		assertThat(userReturned).hasFieldOrPropertyWithValue("email", "4@google.com");
		assertThat(userReturned).hasFieldOrPropertyWithValue("name", "the fourth");
		assertThat(userFromDb).isNotNull();
		assertThat(userFromDb).isEqualTo(userReturned);
		assertThat(userFromDb).hasFieldOrPropertyWithValue("id", 4);
		assertThat(userFromDb).hasFieldOrPropertyWithValue("email", "4@google.com");
		assertThat(userReturned).hasFieldOrPropertyWithValue("name", "the fourth");
	}

	@Test
	public void getAllGenres() {
		List<Genre> genres = genreDao.getAll();

		assertThat(genres).hasSize(6);
		assertThat(genres.get(0)).isEqualTo(GENRE_COMEDY);
		assertThat(genres.get(1)).isEqualTo(GENRE_DRAMA);
		assertThat(genres.get(2)).isEqualTo(GENRE_CARTOON);
		assertThat(genres.get(3)).isEqualTo(GENRE_THRILLER);
		assertThat(genres.get(4)).isEqualTo(GENRE_DOCUMENTARY);
		assertThat(genres.get(5)).isEqualTo(GENRE_ACTION);
	}

	@Test
	public void getAllGenresWhenEmpty() {
		jdbcTemplate.update("DELETE FROM film_genre");
		jdbcTemplate.update("DELETE FROM genres");
		List<Genre> genres = genreDao.getAll();

		assertThat(genres).hasSize(0);
	}

	@Test
	public void getGenreById() {
		Genre genre = genreDao.getById(1);

		assertThat(genre).isNotNull();
		assertThat(genre).hasFieldOrPropertyWithValue("id", 1);
		assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия");
	}

	@Test
	public void getGenreByNonExistentId() {
		assertThatThrownBy(() -> genreDao.getById(999)).hasMessage("Жанр с id=999 не найден.");
	}

	@Test
	public void getAllGenresByFilmId() {
		Set<Genre> genres = genreDao.getAllByFilmId(3);

		assertThat(genres).hasSize(3);
		assertThat(genres).contains(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION);
	}

	@Test
	public void getAllGenresByFilmIdWhenEmpty() {
		jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = 3");
		Set<Genre> genres = genreDao.getAllByFilmId(3);

		assertThat(genres).hasSize(0);
	}

	@Test
	public void getAllMpa() {
		List<Mpa> mpa = mpaDao.getAll();

		assertThat(mpa).hasSize(5);
		assertThat(mpa.get(0)).isEqualTo(MPA_G);
		assertThat(mpa.get(1)).isEqualTo(MPA_PG);
		assertThat(mpa.get(2)).isEqualTo(MPA_PG_13);
		assertThat(mpa.get(3)).isEqualTo(MPA_R);
		assertThat(mpa.get(4)).isEqualTo(MPA_NC_17);
	}

	@Test
	public void getAllMpaWhenEmpty() {
		jdbcTemplate.update("DELETE FROM film_genre");
		jdbcTemplate.update("DELETE FROM likes");
		jdbcTemplate.update("DELETE FROM films");
		jdbcTemplate.update("DELETE FROM mpa");
		List<Mpa> mpa = mpaDao.getAll();

		assertThat(mpa).hasSize(0);
	}

	@Test
	public void getMpaById() {
		Mpa mpa = mpaDao.getById(1);

		assertThat(mpa).isNotNull();
		assertThat(mpa).hasFieldOrPropertyWithValue("id", 1);
		assertThat(mpa).hasFieldOrPropertyWithValue("name", "G");
	}

	@Test
	public void getMpaByNonExistentId() {
		assertThatThrownBy(() -> mpaDao.getById(999)).hasMessage("Рейтинг с id=999 не найден.");
	}

	@Test
	public void getAllLikesByFilmId() {
		List<Integer> likes = likesDao.getAllByFilmId(2);

		assertThat(likes).hasSize(2);
		assertThat(likes.get(0)).isEqualTo(1);
		assertThat(likes.get(1)).isEqualTo(2);
	}

	@Test
	public void getAllLikesByFilmIdWhenEmpty() {
		jdbcTemplate.update("DELETE FROM likes WHERE film_id = 2");
		List<Integer> likes = likesDao.getAllByFilmId(2);

		assertThat(likes).hasSize(0);
	}

	@Test
	public void addLike() {
		likesDao.save(1, 3);
		Film film = filmStorage.getById(1);
		List<Integer> likes = likesDao.getAllByFilmId(1);

		assertThat(film).hasFieldOrPropertyWithValue("rate", 2);
		assertThat(likes).contains(3);
	}

	@Test
	public void deleteLike() {
		likesDao.delete(1, 2);
		Film film = filmStorage.getById(1);
		List<Integer> likes = likesDao.getAllByFilmId(1);

		assertThat(film).hasFieldOrPropertyWithValue("rate", 0);
		assertThat(likes).hasSize(0);
	}

	@Test
	public void getTopLikesAllFilms() {
		List<Film> topLikes = likesDao.getTop(10);

		assertThat(topLikes).hasSize(3);
		assertThat(topLikes.get(0)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R));
		assertThat(topLikes.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G));
		assertThat(topLikes.get(2)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG));
	}

	@Test
	public void getTopLikesFewerThanFilms() {
		List<Film> topLikes = likesDao.getTop(2);

		assertThat(topLikes).hasSize(2);
		assertThat(topLikes.get(0)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R));
		assertThat(topLikes.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G));
	}

	@Test
	public void getTopLikesWhenSomeWithNoLikes() {
		jdbcTemplate.update("DELETE FROM likes WHERE film_id IN (1, 2)");
		List<Film> topLikes = likesDao.getTop(10);

		assertThat(topLikes).hasSize(3);
		assertThat(topLikes.get(0)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R));
		assertThat(topLikes.get(1)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 0,
				Set.of(GENRE_COMEDY), MPA_PG));
		assertThat(topLikes.get(2)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 0,
				Set.of(GENRE_THRILLER), MPA_G));
	}

	@Test
	public void getTopLikesWhenAllWithNoLikes() {
		jdbcTemplate.update("DELETE FROM likes");
		List<Film> topLikes = likesDao.getTop(10);

		assertThat(topLikes).hasSize(3);
		assertThat(topLikes.get(0)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 0,
				Set.of(GENRE_COMEDY), MPA_PG));
		assertThat(topLikes.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 0,
				Set.of(GENRE_THRILLER), MPA_G));
		assertThat(topLikes.get(2)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 0,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R));
	}

	@Test
	public void getAllFriendsById() {
		List<User> friends = friendsDao.getAllById(1);

		assertThat(friends).hasSize(2);
		assertThat(friends.get(0)).isEqualTo(new User(2, "2@yandex.ru", "user2", "second",
				LocalDate.of(1994, 10, 14)));
		assertThat(friends.get(1)).isEqualTo(new User(3, "3@yandex.ru", "user3", "third",
				LocalDate.of(1996, 6, 20)));
	}

	@Test
	public void getAllFriendsByIdWhenEmpty() {
		jdbcTemplate.update("DELETE FROM friends WHERE user_id = 1");
		List<User> friends = friendsDao.getAllById(1);

		assertThat(friends).hasSize(0);
	}

	@Test
	public void addFriend() {
		friendsDao.save(2, 1);
		List<User> friends = friendsDao.getAllById(2);

		assertThat(friends).hasSize(2);
		assertThat(friends.get(0)).isEqualTo(new User(3, "3@yandex.ru", "user3", "third",
				LocalDate.of(1996, 6, 20)));
		assertThat(friends.get(1)).isEqualTo(new User(1, "1@yandex.ru", "user1", "first",
				LocalDate.of(1992, 3, 4)));
	}

	@Test
	public void deleteFriend() {
		friendsDao.delete(1, 3);
		List<User> friends = friendsDao.getAllById(1);

		assertThat(friends).hasSize(1);
		assertThat(friends.get(0)).isEqualTo(new User(2, "2@yandex.ru", "user2", "second",
				LocalDate.of(1994, 10, 14)));
	}

	@Test
	public void getCommonById() {
		List<User> commonFriends = friendsDao.getCommonById(1, 2);

		assertThat(commonFriends).hasSize(1);
		assertThat(commonFriends.get(0)).isEqualTo(new User(3, "3@yandex.ru", "user3", "third",
				LocalDate.of(1996, 6, 20)));
	}

	@Test
	public void getCommonByIdWhenNoCommon() {
		List<User> commonFriends = friendsDao.getCommonById(2, 3);

		assertThat(commonFriends).hasSize(0);
	}

	@Test
	public void getCommonByIdWhenEmpty() {
		jdbcTemplate.update("DELETE FROM friends");
		List<User> commonFriends = friendsDao.getCommonById(1, 2);

		assertThat(commonFriends).hasSize(0);
	}
}