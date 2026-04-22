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
        if (movieRepository.count() > 0) {
            return;
        }

        log.info("Database is empty, applying bootstrap seed data");

        Director nolan = directorRepository.save(new Director(null, "Nolan", "Christopher", null, List.of()));
        Director villeneuve = directorRepository.save(new Director(null, "Villeneuve", "Denis", null, List.of()));
        Director miyazaki = directorRepository.save(new Director(null, "Miyazaki", "Hayao", null, List.of()));

        Genre sciFi = genreRepository.save(new Genre(null, "Sci-Fi", Set.of()));
        Genre drama = genreRepository.save(new Genre(null, "Drama", Set.of()));
        Genre adventure = genreRepository.save(new Genre(null, "Adventure", Set.of()));
        Genre fantasy = genreRepository.save(new Genre(null, "Fantasy", Set.of()));
        Genre animation = genreRepository.save(new Genre(null, "Animation", Set.of()));

        Studio warner = studioRepository.save(new Studio(null, "Warner Bros.", "Burbank, California", List.of()));
        Studio legendary =
                studioRepository.save(new Studio(null, "Legendary Pictures", "Burbank, California", List.of()));
        Studio ghibli = studioRepository.save(new Studio(null, "Studio Ghibli", "Koganei, Tokyo", List.of()));

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

        log.info("Bootstrap seed applied: {} movies, {} reviews", movieRepository.count(), reviewRepository.count());
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
