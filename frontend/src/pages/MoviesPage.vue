<template>
  <section>
    <div class="page-head">
      <div>
        <h2>Movies</h2>
      </div>
      <button class="btn-primary" type="button" @click="openCreateMovie">Create movie</button>
    </div>

    <div class="list-toolbar list-toolbar--movies">
      <div class="list-toolbar-summary">
        <span class="list-toolbar-label">Sort by</span>
        <strong>{{ currentSortLabel }}</strong>
      </div>

      <div class="field list-toolbar-field">
        <label for="movies-direction">Direction</label>
        <SuggestInput
          id="movies-direction"
          v-model="directionInput"
          :suggestions="directionSuggestions"
          placeholder="asc"
          @blur="applyDisplaySettings"
        />
      </div>

      <div class="field list-toolbar-field">
        <label for="movies-size">Page size</label>
        <SuggestInput
          id="movies-size"
          v-model="sizeInput"
          :suggestions="sizeSuggestions"
          placeholder="10"
          @blur="applyDisplaySettings"
        />
      </div>
    </div>

    <div class="movies-layout">
      <aside class="movies-sidebar">
        <div class="sidebar-card">
          <h3>Filters</h3>
          <div class="field">
            <label for="filter-title">Title</label>
            <SuggestInput
              id="filter-title"
              v-model="filters.title"
              :suggestions="movieTitleHints"
              placeholder="Interstellar"
              @blur="applyFilters"
            />
          </div>

          <div class="field">
            <label for="filter-director">Director last name</label>
            <SuggestInput
              id="filter-director"
              v-model="filters.directorLastName"
              :suggestions="directorLastNameHints"
              placeholder="Nolan"
              @blur="applyFilters"
            />
          </div>

          <div class="field">
            <label for="filter-genre">Genre</label>
            <SuggestInput
              id="filter-genre"
              v-model="filters.genreName"
              :suggestions="genreNameHints"
              placeholder="Sci-Fi"
              @blur="applyFilters"
            />
          </div>

          <div class="field">
            <label for="filter-studio">Studio</label>
            <SuggestInput
              id="filter-studio"
              v-model="filters.studioTitle"
              :suggestions="studioTitleHints"
              placeholder="Warner"
              @blur="applyFilters"
            />
          </div>

          <div class="form-row sidebar-actions">
            <button class="btn-outline" type="button" @click="resetFilters">Reset</button>
          </div>
        </div>
      </aside>

      <div class="movies-main">
        <div class="table-wrap">
          <table class="table table--movies">
            <colgroup>
              <col class="table-col-poster" />
              <col class="table-col-title" />
              <col class="table-col-year" />
              <col class="table-col-rating" />
              <col class="table-col-director" />
              <col class="table-col-studio" />
              <col class="table-col-genres" />
              <col class="table-col-actions" />
            </colgroup>
            <thead>
              <tr>
                <th class="table-head-center">Poster</th>
                <th>
                  <button
                    class="table-sort-button"
                    :class="{ 'is-active': paging.sort === 'title' }"
                    type="button"
                    @click="toggleMovieSort('title')"
                  >
                    <span>Title</span>
                    <span class="table-sort-indicator">{{ movieSortIndicator('title') }}</span>
                  </button>
                </th>
                <th class="table-head-center">
                  <button
                    class="table-sort-button"
                    :class="{ 'is-active': paging.sort === 'year' }"
                    type="button"
                    @click="toggleMovieSort('year')"
                  >
                    <span>Year</span>
                    <span class="table-sort-indicator">{{ movieSortIndicator('year') }}</span>
                  </button>
                </th>
                <th class="table-head-center">
                  <button
                    class="table-sort-button"
                    :class="{ 'is-active': paging.sort === 'averageRating' }"
                    type="button"
                    @click="toggleMovieSort('averageRating')"
                  >
                    <span>Rating</span>
                    <span class="table-sort-indicator">{{ movieSortIndicator('averageRating') }}</span>
                  </button>
                </th>
                <th>
                  <button
                    class="table-sort-button"
                    :class="{ 'is-active': paging.sort === 'directorLastName' }"
                    type="button"
                    @click="toggleMovieSort('directorLastName')"
                  >
                    <span>Director</span>
                    <span class="table-sort-indicator">{{ movieSortIndicator('directorLastName') }}</span>
                  </button>
                </th>
                <th>
                  <button
                    class="table-sort-button"
                    :class="{ 'is-active': paging.sort === 'studioTitle' }"
                    type="button"
                    @click="toggleMovieSort('studioTitle')"
                  >
                    <span>Studio</span>
                    <span class="table-sort-indicator">{{ movieSortIndicator('studioTitle') }}</span>
                  </button>
                </th>
                <th>Genres</th>
                <th class="table-head-center">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="movie in movies"
                :key="movie.id"
                class="movie-row"
                tabindex="0"
                role="link"
                @click="openMovieDetails(movie.id)"
                @keydown.enter.prevent="openMovieDetails(movie.id)"
                @keydown.space.prevent="openMovieDetails(movie.id)"
              >
                <td class="table-cell-center table-cell-poster">
                  <img class="poster-thumb" :src="posterUrl(movie.posterUrl)" :alt="movie.title" />
                </td>
                <td>
                  <div class="movie-title-cell">
                    <strong>{{ movie.title }}</strong>
                    <div class="movie-title-meta">Views: {{ movie.viewCount }}</div>
                  </div>
                </td>
                <td class="table-cell-center">{{ movie.year }}</td>
                <td class="table-cell-center">
                  <div class="movie-rating-cell" :class="{ 'is-empty': !movie.reviewCount }">
                    <strong class="movie-rating-value">{{ formatRating(movie) }}</strong>
                    <span class="movie-rating-note">{{ formatReviewCount(movie) }}</span>
                  </div>
                </td>
                <td>
                  <button
                    v-if="movie.directorLastName"
                    class="movie-filter-link"
                    type="button"
                    @click.stop="showMoviesByDirector(movie)"
                  >
                    {{ directorLabel(movie) }}
                  </button>
                  <span v-else>—</span>
                </td>
                <td>
                  <button
                    v-if="movie.studioTitle"
                    class="movie-filter-link"
                    type="button"
                    @click.stop="showMoviesByStudio(movie)"
                  >
                    {{ movie.studioTitle }}
                  </button>
                  <span v-else>—</span>
                </td>
                <td>
                  <div class="chips">
                    <button
                      v-for="genre in movie.genres || []"
                      :key="genre.id"
                      class="chip chip-link"
                      type="button"
                      @click.stop="showMoviesByGenre(genre)"
                    >
                      {{ genre.name }}
                    </button>
                    <span v-if="!(movie.genres || []).length" style="color: var(--ink-muted)">—</span>
                  </div>
                </td>
                <td class="table-cell-actions">
                  <div class="table-actions table-actions--icon">
                    <button
                      class="icon-action-button"
                      type="button"
                      title="Edit movie"
                      aria-label="Edit movie"
                      @click.stop="openEditMovie(movie)"
                    >
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path
                          d="M10.33 1.83 9.7 4.36a7.97 7.97 0 0 0-1.73.72L5.7 3.8 3.8 5.7l1.28 2.27c-.3.55-.54 1.13-.72 1.73l-2.53.63v2.68l2.53.63c.18.6.42 1.18.72 1.73L3.8 18.3l1.9 1.9 2.27-1.28c.55.3 1.13.54 1.73.72l.63 2.53h2.68l.63-2.53a7.97 7.97 0 0 0 1.73-.72l2.27 1.28 1.9-1.9-1.28-2.27c.3-.55.54-1.13.72-1.73l2.53-.63v-2.68l-2.53-.63a7.97 7.97 0 0 0-.72-1.73L20.2 5.7l-1.9-1.9-2.27 1.28a7.97 7.97 0 0 0-1.73-.72l-.63-2.53z"
                        />
                        <circle cx="12" cy="12" r="3.25" />
                      </svg>
                    </button>
                    <button
                      class="icon-action-button icon-action-button--delete"
                      type="button"
                      title="Delete movie"
                      aria-label="Delete movie"
                      @click.stop="openDeleteModal(movie)"
                    >
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M6 6 18 18" />
                        <path d="M18 6 6 18" />
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="movies.length === 0 && !loading">
                <td colspan="8">No movies found.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <PaginationBar
          :page="paging.page"
          :total-pages="paging.totalPages"
          :total-elements="paging.totalElements"
          @change="loadMovies"
        />

        <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>
      </div>
    </div>

    <MovieFormModal
      v-model="movieFormVisible"
      :movie="selectedMovie"
      :directors="directors"
      :genres="genres"
      :studios="studios"
      :title-suggestions="movieTitleHints"
      :loading="movieFormLoading"
      :error-message="movieFormError"
      @submit="submitMovie"
      @open-poster="openPosterFromEdit"
    />

    <PosterModal
      v-if="posterMovie"
      v-model="posterModalVisible"
      :movie-id="posterMovie.id"
      :movie-title="posterMovie.title"
      @updated="refreshAfterPosterUpdate"
    />

    <ConfirmModal
      v-model="deleteModalVisible"
      title="Delete movie"
      :message="deleteMovieMessage"
      :loading="deleteLoading"
      @confirm="confirmDelete"
    />

  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ConfirmModal from '../components/ConfirmModal.vue';
import MovieFormModal from '../components/MovieFormModal.vue';
import PaginationBar from '../components/PaginationBar.vue';
import PosterModal from '../components/PosterModal.vue';
import SuggestInput from '../components/SuggestInput.vue';
import {
  buildMovieFiltersQuery,
  directorMovieFilters,
  genreMovieFilters,
  readMovieFiltersFromQuery,
  studioMovieFilters
} from '../lib/movieFilters';
import {
  createMovie,
  deleteMovie,
  formatApiError,
  getDirectors,
  getGenres,
  getMovieById,
  getMoviesSearch,
  getStudios,
  toApiUrl,
  updateMovie
} from '../lib/api';
import { sortIndicator, sortLabel, toggleSortState } from '../lib/tableSort';

const route = useRoute();
const router = useRouter();

const movies = ref([]);
const directors = ref([]);
const genres = ref([]);
const studios = ref([]);

const loading = ref(false);
const errorMessage = ref('');

const filters = reactive({
  title: '',
  directorLastName: '',
  genreName: '',
  studioTitle: ''
});

const paging = reactive({
  page: 0,
  size: 10,
  sort: 'title',
  direction: 'asc',
  totalPages: 0,
  totalElements: 0
});

const movieFormVisible = ref(false);
const movieFormLoading = ref(false);
const movieFormError = ref('');
const selectedMovie = ref(null);

const deleteModalVisible = ref(false);
const deleteLoading = ref(false);
const movieToDelete = ref(null);
const deleteMovieMessage = ref('');

const posterModalVisible = ref(false);
const posterMovie = ref(null);

const allMovieTitles = ref([]);

const movieTitleHints = computed(() =>
  uniqueTextValues([
    ...allMovieTitles.value,
    ...movies.value.map((movie) => movie.title)
  ])
);
const directorLastNameHints = computed(() =>
  uniqueTextValues(directors.value.map((director) => director.lastName))
);
const genreNameHints = computed(() =>
  uniqueTextValues(genres.value.map((genre) => genre.name))
);
const studioTitleHints = computed(() =>
  uniqueTextValues(studios.value.map((studio) => studio.title))
);
const directionSuggestions = ['asc', 'desc'];
const sizeSuggestions = ['5', '10', '20'];
const movieSortLabels = {
  title: 'Title',
  year: 'Year',
  averageRating: 'Rating',
  directorLastName: 'Director',
  studioTitle: 'Studio',
  viewCount: 'Views',
  id: 'ID'
};
const currentSortLabel = computed(() => sortLabel(paging.sort, movieSortLabels));

const directionInput = computed({
  get: () => paging.direction,
  set: (value) => {
    paging.direction = `${value || ''}`.trim();
  }
});

const sizeInput = computed({
  get: () => `${paging.size}`,
  set: (value) => {
    const parsed = Number.parseInt(`${value || ''}`.trim(), 10);
    if (Number.isInteger(parsed) && parsed > 0) {
      paging.size = parsed;
    }
  }
});

onMounted(async () => {
  await Promise.all([loadReferenceData(), loadMovieTitleHints()]);
});

watch(
  () => route.query,
  async (query) => {
    applyFiltersFromQuery(query);
    await loadMovies(0);
  },
  { immediate: true }
);

function posterUrl(path) {
  return toApiUrl(path) ||
    'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="300"%3E%3Crect fill="%23d8e4e7" width="100%25" height="100%25"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-size="16" fill="%2360707f"%3ENo Poster%3C/text%3E%3C/svg%3E';
}

function directorLabel(movie) {
  const parts = [movie.directorLastName, movie.directorFirstName, movie.directorMiddleName].filter(Boolean);
  return parts.length ? parts.join(' ') : '—';
}

function formatRating(movie) {
  if (!movie?.reviewCount || movie.averageRating == null) {
    return '—';
  }
  return `${Number(movie.averageRating).toFixed(1)} / 10`;
}

function formatReviewCount(movie) {
  const count = Number(movie?.reviewCount || 0);
  if (count === 0) {
    return 'No reviews';
  }
  if (count === 1) {
    return '1 review';
  }
  return `${count} reviews`;
}

async function loadReferenceData() {
  try {
    const [allDirectors, allGenres, allStudios] = await Promise.all([
      loadAllItems((page) =>
        getDirectors({ page, size: 100, sort: 'lastName', direction: 'asc' })
      ),
      loadAllItems((page) =>
        getGenres({ page, size: 100, sort: 'name', direction: 'asc' })
      ),
      loadAllItems((page) =>
        getStudios({ page, size: 100, sort: 'title', direction: 'asc' })
      )
    ]);
    directors.value = allDirectors;
    genres.value = allGenres;
    studios.value = allStudios;
  } catch (error) {
    errorMessage.value = formatApiError(error);
  }
}

async function loadMovieTitleHints() {
  try {
    const allMovies = await loadAllItems((page) =>
      getMoviesSearch({
        page,
        size: 100,
        sort: 'title',
        direction: 'asc',
        native: false
      })
    );

    allMovieTitles.value = allMovies.map((movie) => movie.title).filter(Boolean);
  } catch {
    allMovieTitles.value = [];
  }
}

function applyFiltersFromQuery(query) {
  const nextFilters = readMovieFiltersFromQuery(query);
  filters.title = nextFilters.title;
  filters.directorLastName = nextFilters.directorLastName;
  filters.genreName = nextFilters.genreName;
  filters.studioTitle = nextFilters.studioTitle;
}

function currentFilterQuery() {
  return buildMovieFiltersQuery(filters);
}

function hasSameRouteFilters(query) {
  return JSON.stringify(buildMovieFiltersQuery(route.query)) === JSON.stringify(query);
}

async function loadMovies(targetPage = paging.page, options = {}) {
  const { syncRoute = false } = options;

  if (syncRoute) {
    const nextQuery = currentFilterQuery();
    if (!hasSameRouteFilters(nextQuery)) {
      await router.push({ path: '/movies', query: nextQuery });
      return;
    }
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await getMoviesSearch({
      ...filters,
      page: targetPage,
      size: paging.size,
      sort: paging.sort,
      direction: paging.direction,
      native: false
    });

    paging.page = response.number;
    paging.totalPages = response.totalPages;
    paging.totalElements = response.totalElements;

    const detailedMovies = await Promise.all(
      (response.content || []).map(async (movie) => {
        try {
          return await getMovieById(movie.id);
        } catch {
          return movie;
        }
      })
    );

    movies.value = detailedMovies;
  } catch (error) {
    errorMessage.value = formatApiError(error);
  } finally {
    loading.value = false;
  }
}

function applyFilters() {
  loadMovies(0, { syncRoute: true });
}

function applyDisplaySettings() {
  loadMovies(0);
}

function toggleMovieSort(field) {
  toggleSortState(paging, field);
  loadMovies(0);
}

function movieSortIndicator(field) {
  return sortIndicator(paging, field);
}

function uniqueTextValues(values) {
  const normalizedToOriginal = new Map();
  values.forEach((value) => {
    const normalized = `${value || ''}`.trim();
    if (!normalized) {
      return;
    }
    const key = normalized.toLocaleLowerCase();
    if (!normalizedToOriginal.has(key)) {
      normalizedToOriginal.set(key, normalized);
    }
  });
  return Array.from(normalizedToOriginal.values()).sort((a, b) =>
    a.localeCompare(b, undefined, { sensitivity: 'base' })
  );
}

async function loadAllItems(fetchPage) {
  const items = [];
  let pageIndex = 0;

  while (true) {
    const pageData = await fetchPage(pageIndex);
    const content = pageData.content || [];
    items.push(...content);

    pageIndex += 1;
    if (pageIndex >= (pageData.totalPages || 0) || content.length === 0) {
      break;
    }
  }

  return items;
}

function resetFilters() {
  filters.title = '';
  filters.directorLastName = '';
  filters.genreName = '';
  filters.studioTitle = '';
  applyFilters();
}

function goToMoviesWithFilters(nextFilters) {
  const nextQuery = buildMovieFiltersQuery(nextFilters);
  if (hasSameRouteFilters(nextQuery)) {
    loadMovies(0);
    return;
  }
  router.push({ path: '/movies', query: nextQuery });
}

function showMoviesByDirector(movie) {
  goToMoviesWithFilters(directorMovieFilters(movie));
}

function showMoviesByGenre(genre) {
  goToMoviesWithFilters(genreMovieFilters(genre));
}

function showMoviesByStudio(movie) {
  goToMoviesWithFilters(studioMovieFilters(movie));
}

function openCreateMovie() {
  selectedMovie.value = null;
  movieFormError.value = '';
  movieFormVisible.value = true;
}

function openEditMovie(movie) {
  selectedMovie.value = movie;
  movieFormError.value = '';
  movieFormVisible.value = true;
}

async function submitMovie(payload) {
  movieFormLoading.value = true;
  movieFormError.value = '';
  try {
    if (selectedMovie.value) {
      await updateMovie(selectedMovie.value.id, payload);
    } else {
      await createMovie(payload);
    }
    movieFormVisible.value = false;
    await loadMovies(selectedMovie.value ? paging.page : 0);
  } catch (error) {
    movieFormError.value = formatApiError(error);
  } finally {
    movieFormLoading.value = false;
  }
}

function openDeleteModal(movie) {
  movieToDelete.value = movie;
  deleteMovieMessage.value = `Delete movie '${movie.title}'?`;
  deleteModalVisible.value = true;
}

async function confirmDelete() {
  if (!movieToDelete.value) {
    return;
  }
  deleteLoading.value = true;
  errorMessage.value = '';
  try {
    await deleteMovie(movieToDelete.value.id);
    deleteModalVisible.value = false;
    await loadMovies(0);
  } catch (error) {
    errorMessage.value = formatApiError(error);
  } finally {
    deleteLoading.value = false;
  }
}

function openPosterModal(movie) {
  posterMovie.value = movie;
  posterModalVisible.value = true;
}

function openPosterFromEdit() {
  if (!selectedMovie.value) {
    return;
  }
  openPosterModal(selectedMovie.value);
}

function openMovieDetails(movieId) {
  router.push({
    path: `/movies/${movieId}`,
    query: buildMovieFiltersQuery(route.query)
  });
}

async function refreshAfterPosterUpdate() {
  await loadMovies(paging.page);
}
</script>
