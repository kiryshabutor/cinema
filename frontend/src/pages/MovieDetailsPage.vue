<template>
  <section class="movie-details-page">
    <RouterLink class="movie-back-link" :to="backToMoviesLocation">
      <span aria-hidden="true">&larr;</span>
      <span>Back to movies</span>
    </RouterLink>

    <div class="page-head movie-details-head">
      <div class="movie-details-title">
        <h2>Movie details</h2>
      </div>
    </div>

    <div v-if="movie" class="detail-layout movie-details-card">
      <img class="poster-thumb" :src="posterUrl(movie.posterUrl)" :alt="movie.title" />
      <div class="movie-details-copy">
        <h3>{{ movie.title }} ({{ movie.year }})</h3>
        <div class="movie-details-rating" :class="{ 'is-empty': !movie.reviewCount }">
          <strong>{{ formatRating(movie) }}</strong>
          <span>{{ formatReviewCount(movie) }}</span>
        </div>
        <div class="movie-details-facts">
          <p><strong>Duration:</strong> {{ movie.duration }} min</p>
          <p><strong>Views:</strong> {{ movie.viewCount }}</p>
          <p>
            <strong>Director: </strong>
            <RouterLink
              v-if="movie.directorLastName"
              class="movie-filter-link"
              :to="directorMoviesLocation(movie)"
            >
              {{ directorLabel(movie) }}
            </RouterLink>
            <span v-else>—</span>
          </p>
          <p>
            <strong>Studio: </strong>
            <RouterLink
              v-if="movie.studioTitle"
              class="movie-filter-link"
              :to="studioMoviesLocation(movie)"
            >
              {{ movie.studioTitle }}
            </RouterLink>
            <span v-else>—</span>
          </p>
        </div>
        <div class="movie-details-genres">
          <strong>Genres:</strong>
          <div class="chips">
            <RouterLink
              v-for="genre in movie.genres || []"
              :key="genre.id"
              class="chip chip-link"
              :to="genreMoviesLocation(genre)"
            >
              {{ genre.name }}
            </RouterLink>
            <span v-if="!(movie.genres || []).length" class="movie-details-empty-state">No genres</span>
          </div>
        </div>
      </div>
    </div>

    <div class="movie-details-reviews">
      <div class="movie-details-reviews-head">
        <div>
          <h3>Reviews</h3>
          <p>{{ reviewsPaging.totalElements }} total</p>
        </div>
        <button class="btn-primary movie-details-review-button" type="button" @click="reviewModalVisible = true">
          Add review
        </button>
      </div>

      <div class="reviews-list movie-details-reviews-list">
        <article v-for="review in reviews" :key="review.id" class="review-item movie-details-review-card">
          <strong>{{ review.authorAlias }} • Rating: {{ review.rating }}/10</strong>
          <div>{{ review.comment || 'No comment' }}</div>
        </article>
        <article v-if="reviews.length === 0" class="review-item movie-details-review-card">No reviews yet.</article>
      </div>

      <PaginationBar
        :page="reviewsPaging.page"
        :total-pages="reviewsPaging.totalPages"
        :total-elements="reviewsPaging.totalElements"
        @change="loadReviews"
      />
    </div>

    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <ReviewFormModal
      v-model="reviewModalVisible"
      :loading="reviewSaving"
      :error-message="reviewError"
      @submit="submitReview"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import PaginationBar from '../components/PaginationBar.vue';
import ReviewFormModal from '../components/ReviewFormModal.vue';
import {
  buildMovieFiltersQuery,
  directorMovieFilters,
  genreMovieFilters,
  studioMovieFilters
} from '../lib/movieFilters';
import {
  createReview,
  formatApiError,
  getMovieById,
  getMovieReviews,
  toApiUrl
} from '../lib/api';

const route = useRoute();
const movieId = Number(route.params.id);
const backToMoviesLocation = computed(() => ({
  path: '/movies',
  query: buildMovieFiltersQuery(route.query)
}));

const movie = ref(null);
const reviews = ref([]);
const errorMessage = ref('');

const reviewsPaging = reactive({
  page: 0,
  totalPages: 0,
  totalElements: 0,
  size: 10
});

const reviewModalVisible = ref(false);
const reviewSaving = ref(false);
const reviewError = ref('');

onMounted(async () => {
  await Promise.all([loadMovie(), loadReviews(0)]);
});

function posterUrl(path) {
  return toApiUrl(path) ||
    'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="300"%3E%3Crect fill="%23d8e4e7" width="100%25" height="100%25"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" font-size="16" fill="%2360707f"%3ENo Poster%3C/text%3E%3C/svg%3E';
}

function directorLabel(currentMovie) {
  const parts = [
    currentMovie.directorLastName,
    currentMovie.directorFirstName,
    currentMovie.directorMiddleName
  ].filter(Boolean);
  return parts.length ? parts.join(' ') : '—';
}

function directorMoviesLocation(currentMovie) {
  return {
    path: '/movies',
    query: buildMovieFiltersQuery(directorMovieFilters(currentMovie))
  };
}

function studioMoviesLocation(currentMovie) {
  return {
    path: '/movies',
    query: buildMovieFiltersQuery(studioMovieFilters(currentMovie))
  };
}

function genreMoviesLocation(genre) {
  return {
    path: '/movies',
    query: buildMovieFiltersQuery(genreMovieFilters(genre))
  };
}

function formatRating(currentMovie) {
  if (!currentMovie?.reviewCount || currentMovie.averageRating == null) {
    return 'No rating yet';
  }
  return `${Number(currentMovie.averageRating).toFixed(1)} / 10`;
}

function formatReviewCount(currentMovie) {
  const count = Number(currentMovie?.reviewCount || 0);
  if (count === 0) {
    return 'Add the first review';
  }
  if (count === 1) {
    return '1 review';
  }
  return `${count} reviews`;
}

async function loadMovie() {
  errorMessage.value = '';
  try {
    movie.value = await getMovieById(movieId);
  } catch (error) {
    errorMessage.value = formatApiError(error);
  }
}

async function loadReviews(targetPage = reviewsPaging.page) {
  errorMessage.value = '';
  try {
    const page = await getMovieReviews(movieId, {
      page: targetPage,
      size: reviewsPaging.size,
      sort: 'id',
      direction: 'desc'
    });

    reviews.value = page.content || [];
    reviewsPaging.page = page.number;
    reviewsPaging.totalPages = page.totalPages;
    reviewsPaging.totalElements = page.totalElements;
  } catch (error) {
    errorMessage.value = formatApiError(error);
  }
}

async function submitReview(payload) {
  reviewSaving.value = true;
  reviewError.value = '';
  try {
    await createReview({
      ...payload,
      movieId
    });
    reviewModalVisible.value = false;
    await Promise.all([loadMovie(), loadReviews(0)]);
  } catch (error) {
    reviewError.value = formatApiError(error);
  } finally {
    reviewSaving.value = false;
  }
}
</script>
