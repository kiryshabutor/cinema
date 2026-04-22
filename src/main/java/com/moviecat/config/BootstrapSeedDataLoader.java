package com.moviecat.config;

import com.moviecat.model.Director;
import com.moviecat.model.Genre;
import com.moviecat.model.Movie;
import com.moviecat.model.Review;
import com.moviecat.model.Studio;
import com.moviecat.repository.DirectorRepository;
import com.moviecat.repository.GenreRepository;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.ReviewRepository;
import com.moviecat.repository.StudioRepository;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.bootstrap.seed.enabled", havingValue = "true", matchIfMissing = true)
public class BootstrapSeedDataLoader implements ApplicationRunner {

    private final DirectorRepository directorRepository;
    private final GenreRepository genreRepository;
    private final StudioRepository studioRepository;
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long movieCount = movieRepository.count();
        if (movieCount > 5) {
            log.info("Bootstrap seed skipped because database already contains {} movies", movieCount);
            return;
        }

        if (movieCount == 0) {
            log.info("Database is empty, applying bootstrap seed data");
            seedInitialCatalog();
            movieCount = movieRepository.count();
        }

        if (movieCount == 5) {
            log.info("Database contains initial 5-movie dataset, applying expansion seed data");
            seedCatalogExpansion();
        } else if (movieCount > 0) {
            log.info("Bootstrap seed skipped because database contains {} movies", movieCount);
            return;
        }

        log.info("Bootstrap seed applied: {} movies, {} reviews", movieRepository.count(), reviewRepository.count());
    }

    private void seedInitialCatalog() {
        Director nolan = saveDirector("Nolan", "Christopher", null);
        Director villeneuve = saveDirector("Villeneuve", "Denis", null);
        Director miyazaki = saveDirector("Miyazaki", "Hayao", null);

        Genre sciFi = saveGenre("Sci-Fi");
        Genre drama = saveGenre("Drama");
        Genre adventure = saveGenre("Adventure");
        Genre fantasy = saveGenre("Fantasy");
        Genre animation = saveGenre("Animation");

        Studio warner = saveStudio("Warner Bros.", "Burbank, California");
        Studio legendary = saveStudio("Legendary Pictures", "Burbank, California");
        Studio ghibli = saveStudio("Studio Ghibli", "Koganei, Tokyo");

        Movie interstellar = saveMovie(
                "Interstellar",
                2014,
                169,
                5421L,
                nolan,
                warner,
                Set.of(sciFi, drama, adventure));
        Movie inception = saveMovie(
                "Inception",
                2010,
                148,
                4875L,
                nolan,
                warner,
                Set.of(sciFi, drama, adventure));
        Movie dune = saveMovie(
                "Dune",
                2021,
                155,
                3612L,
                villeneuve,
                legendary,
                Set.of(sciFi, drama, adventure));
        Movie bladeRunner2049 = saveMovie(
                "Blade Runner 2049",
                2017,
                164,
                2930L,
                villeneuve,
                legendary,
                Set.of(sciFi, drama));
        Movie spiritedAway = saveMovie(
                "Spirited Away",
                2001,
                125,
                4188L,
                miyazaki,
                ghibli,
                Set.of(animation, fantasy, adventure));

        reviewRepository.saveAll(List.of(
                review("astro_fan", 10, "Still one of the strongest space epics.", interstellar),
                review("wormhole_77", 9, "Big scale, emotional core, great score.", interstellar),
                review("dream_heist", 10, "Concept-heavy and still very watchable.", inception),
                review("totem_keeper", 9, "Great cast and pacing.", inception),
                review("arrakis_reader", 9, "Atmosphere and visuals carry it hard.", dune),
                review("spice_runner", 8, "Slow but very polished.", dune),
                review("replicant_x", 10, "Looks incredible and lands emotionally.", bladeRunner2049),
                review("deckard_files", 9, "A worthy sequel.", bladeRunner2049),
                review("no_face", 10, "Classic animation with endless replay value.", spiritedAway),
                review("bathhouse_guest", 10, "Inventive, warm and strange in the best way.", spiritedAway)));
    }

    private void seedCatalogExpansion() {
        Director nolan = findOrCreateDirector("Nolan", "Christopher", null);
        Director villeneuve = findOrCreateDirector("Villeneuve", "Denis", null);
        Director miyazaki = findOrCreateDirector("Miyazaki", "Hayao", null);
        Director miller = findOrCreateDirector("Miller", "George", null);
        Director wachowski = findOrCreateDirector("Wachowski", "Lana", null);
        Director jackson = findOrCreateDirector("Jackson", "Peter", null);
        Director bong = findOrCreateDirector("Bong", "Joon-ho", null);

        Genre sciFi = findOrCreateGenre("Sci-Fi");
        Genre drama = findOrCreateGenre("Drama");
        Genre adventure = findOrCreateGenre("Adventure");
        Genre fantasy = findOrCreateGenre("Fantasy");
        Genre animation = findOrCreateGenre("Animation");
        Genre action = findOrCreateGenre("Action");
        Genre thriller = findOrCreateGenre("Thriller");
        Genre crime = findOrCreateGenre("Crime");

        Studio warner = findOrCreateStudio("Warner Bros.", "Burbank, California");
        Studio ghibli = findOrCreateStudio("Studio Ghibli", "Koganei, Tokyo");
        Studio universal = findOrCreateStudio("Universal Pictures", "Universal City, California");
        Studio paramount = findOrCreateStudio("Paramount Pictures", "Los Angeles, California");
        Studio newLine = findOrCreateStudio("New Line Cinema", "Burbank, California");
        Studio cjenm = findOrCreateStudio("CJ ENM", "Seoul, South Korea");

        Movie darkKnight = saveMovieIfMissing(
                "The Dark Knight",
                2008,
                152,
                6120L,
                nolan,
                warner,
                Set.of(action, crime, drama));
        Movie prestige = saveMovieIfMissing(
                "The Prestige",
                2006,
                130,
                2740L,
                nolan,
                warner,
                Set.of(drama, thriller));
        Movie tenet = saveMovieIfMissing(
                "Tenet",
                2020,
                150,
                3180L,
                nolan,
                warner,
                Set.of(sciFi, action, thriller));
        Movie oppenheimer = saveMovieIfMissing(
                "Oppenheimer",
                2023,
                180,
                4580L,
                nolan,
                universal,
                Set.of(drama, thriller));
        Movie arrival = saveMovieIfMissing(
                "Arrival",
                2016,
                116,
                3520L,
                villeneuve,
                paramount,
                Set.of(sciFi, drama));
        Movie prisoners = saveMovieIfMissing(
                "Prisoners",
                2013,
                153,
                2310L,
                villeneuve,
                warner,
                Set.of(drama, thriller, crime));
        Movie princessMononoke = saveMovieIfMissing(
                "Princess Mononoke",
                1997,
                134,
                3950L,
                miyazaki,
                ghibli,
                Set.of(animation, fantasy, adventure));
        Movie howlsMovingCastle = saveMovieIfMissing(
                "Howl's Moving Castle",
                2004,
                119,
                3660L,
                miyazaki,
                ghibli,
                Set.of(animation, fantasy, adventure));
        Movie furyRoad = saveMovieIfMissing(
                "Mad Max: Fury Road",
                2015,
                120,
                4410L,
                miller,
                warner,
                Set.of(action, adventure, sciFi));
        Movie matrix = saveMovieIfMissing(
                "The Matrix",
                1999,
                136,
                5900L,
                wachowski,
                warner,
                Set.of(sciFi, action));
        Movie fellowship = saveMovieIfMissing(
                "The Lord of the Rings: The Fellowship of the Ring",
                2001,
                178,
                5230L,
                jackson,
                newLine,
                Set.of(fantasy, adventure, drama));
        Movie parasite = saveMovieIfMissing(
                "Parasite",
                2019,
                132,
                4070L,
                bong,
                cjenm,
                Set.of(drama, thriller));

        reviewRepository.saveAll(List.of(
                review("gotham_guard", 10, "Still one of the cleanest comic-book crime dramas.", darkKnight),
                review("tesla_coil", 9, "Tight rivalry story with a great final reveal.", prestige),
                review("time_inverter", 8, "Chaotic in places, but the scale is worth it.", tenet),
                review("chain_reaction", 10, "Serious, tense and consistently well-acted.", oppenheimer),
                review("linguist_loop", 10, "Thoughtful sci-fi that stays emotional.", arrival),
                review("case_file_22", 9, "Heavy tone, excellent performances, no wasted scenes.", prisoners),
                review("forest_spirit", 10, "Huge world, huge heart, and zero filler.", princessMononoke),
                review("castle_walker", 9, "Warm, weird and visually rich all the way through.", howlsMovingCastle),
                review("chrome_engine", 10, "Pure momentum with almost absurd craft.", furyRoad),
                review("red_pill_user", 10, "Still feels sharp and influential.", matrix),
                review("shire_bound", 10, "Massive adventure that never loses intimacy.", fellowship),
                review("basement_light", 10, "Precise, uncomfortable and very rewatchable.", parasite)));
    }

    private Director findOrCreateDirector(String lastName, String firstName, String middleName) {
        return directorRepository.findAll().stream()
                .filter(director -> normalize(director.getLastName()).equals(normalize(lastName))
                        && normalize(director.getFirstName()).equals(normalize(firstName))
                        && normalize(director.getMiddleName()).equals(normalize(middleName)))
                .findFirst()
                .orElseGet(() -> saveDirector(lastName, firstName, middleName));
    }

    private Genre findOrCreateGenre(String name) {
        return genreRepository.findAll().stream()
                .filter(genre -> normalize(genre.getName()).equals(normalize(name)))
                .findFirst()
                .orElseGet(() -> saveGenre(name));
    }

    private Studio findOrCreateStudio(String title, String address) {
        return studioRepository.findAll().stream()
                .filter(studio -> normalize(studio.getTitle()).equals(normalize(title)))
                .findFirst()
                .orElseGet(() -> saveStudio(title, address));
    }

    private Movie saveMovieIfMissing(
            String title,
            int year,
            int duration,
            long viewCount,
            Director director,
            Studio studio,
            Set<Genre> genres) {
        return movieRepository.findAll().stream()
                .filter(movie -> normalize(movie.getTitle()).equals(normalize(title)))
                .findFirst()
                .orElseGet(() -> saveMovie(title, year, duration, viewCount, director, studio, genres));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private Director saveDirector(String lastName, String firstName, String middleName) {
        Director director = new Director();
        director.setLastName(lastName);
        director.setFirstName(firstName);
        director.setMiddleName(middleName);
        return directorRepository.save(director);
    }

    private Genre saveGenre(String name) {
        Genre genre = new Genre();
        genre.setName(name);
        return genreRepository.save(genre);
    }

    private Studio saveStudio(String title, String address) {
        Studio studio = new Studio();
        studio.setTitle(title);
        studio.setAddress(address);
        return studioRepository.save(studio);
    }

    private Movie saveMovie(
            String title,
            int year,
            int duration,
            long viewCount,
            Director director,
            Studio studio,
            Set<Genre> genres) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setYear(year);
        movie.setDuration(duration);
        movie.setViewCount(viewCount);
        movie.setDirector(director);
        movie.setStudio(studio);
        movie.setGenres(genres);
        return movieRepository.save(movie);
    }

    private Review review(String authorAlias, int rating, String comment, Movie movie) {
        Review review = new Review();
        review.setAuthorAlias(authorAlias);
        review.setRating(rating);
        review.setComment(comment);
        review.setMovie(movie);
        return review;
    }
}
