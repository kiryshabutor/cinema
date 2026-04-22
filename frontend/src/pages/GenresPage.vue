<template>
  <section>
    <div class="page-head">
      <div>
        <h2>Genres</h2>
      </div>
      <div class="page-head-actions">
        <div class="list-toolbar page-head-toolbar">
          <div class="list-toolbar-summary">
            <span class="list-toolbar-label">Sort by</span>
            <strong>{{ currentSortLabel }}</strong>
          </div>

          <div class="field list-toolbar-field">
            <label for="genres-size">Page size</label>
            <SuggestInput
              id="genres-size"
              v-model="sizeInput"
              :suggestions="sizeSuggestions"
              placeholder="10"
              @blur="applyTableSettings"
            />
          </div>
        </div>

        <button class="btn-primary page-head-action-button" type="button" @click="openCreate">
          Create genre
        </button>
      </div>
    </div>

    <div class="table-wrap table-wrap--responsive">
      <table class="table table--entity">
        <colgroup>
          <col class="table-col-id" />
          <col class="table-col-title-wide" />
          <col class="table-col-actions-wide" />
        </colgroup>
        <thead>
          <tr>
            <th class="table-head-center">
              <button
                class="table-sort-button"
                :class="{ 'is-active': paging.sort === 'id' }"
                type="button"
                @click="toggleGenreSort('id')"
              >
                <span>ID</span>
                <span class="table-sort-indicator">{{ genreSortIndicator('id') }}</span>
              </button>
            </th>
            <th>
              <button
                class="table-sort-button"
                :class="{ 'is-active': paging.sort === 'name' }"
                type="button"
                @click="toggleGenreSort('name')"
              >
                <span>Name</span>
                <span class="table-sort-indicator">{{ genreSortIndicator('name') }}</span>
              </button>
            </th>
            <th class="table-head-center">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="genre in genres" :key="genre.id">
            <td class="table-cell-center" data-label="ID">{{ genre.id }}</td>
            <td data-label="Name">
              <RouterLink class="movie-filter-link" :to="genreMoviesLocation(genre)">
                {{ genre.name }}
              </RouterLink>
            </td>
            <td class="table-cell-actions" data-label="Actions">
              <div class="table-actions table-actions--icon">
                <button
                  class="icon-action-button"
                  type="button"
                  title="Edit genre"
                  aria-label="Edit genre"
                  @click="openEdit(genre)"
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
                  title="Delete genre"
                  aria-label="Delete genre"
                  @click="openDelete(genre)"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M6 6 18 18" />
                    <path d="M18 6 6 18" />
                  </svg>
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="genres.length === 0 && !loading" class="table-empty-row">
            <td class="table-empty" colspan="3">No genres found.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <PaginationBar
      :page="paging.page"
      :total-pages="paging.totalPages"
      :total-elements="paging.totalElements"
      @change="loadGenres"
    />

    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <GenreFormModal
      v-model="formVisible"
      :genre="selectedGenre"
      :existing-genres="genres"
      :loading="formLoading"
      :error-message="formError"
      @submit="submitGenre"
    />

    <ConfirmModal
      v-model="deleteVisible"
      title="Delete genre"
      :message="deleteMessage"
      :loading="deleteLoading"
      @confirm="confirmDelete"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { RouterLink } from 'vue-router';
import ConfirmModal from '../components/ConfirmModal.vue';
import GenreFormModal from '../components/GenreFormModal.vue';
import PaginationBar from '../components/PaginationBar.vue';
import SuggestInput from '../components/SuggestInput.vue';
import { genreMovieFilters, moviesRouteLocation } from '../lib/movieFilters';
import { sortIndicator, sortLabel, toggleSortState } from '../lib/tableSort';
import { createGenre, deleteGenre, formatApiError, getGenres, updateGenre } from '../lib/api';

const genres = ref([]);
const loading = ref(false);
const errorMessage = ref('');

const paging = reactive({
  page: 0,
  size: 10,
  sort: 'id',
  direction: 'asc',
  totalPages: 0,
  totalElements: 0
});

const sizeSuggestions = ['5', '10', '20'];
const genreSortLabels = {
  id: 'ID',
  name: 'Name'
};
const currentSortLabel = computed(() => sortLabel(paging.sort, genreSortLabels));

const sizeInput = computed({
  get: () => `${paging.size}`,
  set: (value) => {
    const parsed = Number.parseInt(`${value || ''}`.trim(), 10);
    if (Number.isInteger(parsed) && parsed > 0) {
      paging.size = parsed;
    }
  }
});

const formVisible = ref(false);
const formLoading = ref(false);
const formError = ref('');
const selectedGenre = ref(null);

const deleteVisible = ref(false);
const deleteLoading = ref(false);
const deleteTarget = ref(null);
const deleteMessage = ref('');

onMounted(() => {
  loadGenres(0);
});

function genreMoviesLocation(genre) {
  return moviesRouteLocation(genreMovieFilters(genre));
}

function applyTableSettings() {
  loadGenres(0);
}

function toggleGenreSort(field) {
  toggleSortState(paging, field);
  loadGenres(0);
}

function genreSortIndicator(field) {
  return sortIndicator(paging, field);
}

async function loadGenres(targetPage = paging.page) {
  loading.value = true;
  errorMessage.value = '';
  try {
    const page = await getGenres({
      page: targetPage,
      size: paging.size,
      sort: paging.sort,
      direction: paging.direction
    });
    genres.value = page.content || [];
    paging.page = page.number;
    paging.totalPages = page.totalPages;
    paging.totalElements = page.totalElements;
  } catch (error) {
    errorMessage.value = formatApiError(error);
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  selectedGenre.value = null;
  formError.value = '';
  formVisible.value = true;
}

function openEdit(genre) {
  selectedGenre.value = genre;
  formError.value = '';
  formVisible.value = true;
}

async function submitGenre(payload) {
  formLoading.value = true;
  formError.value = '';
  try {
    if (selectedGenre.value) {
      await updateGenre(selectedGenre.value.id, payload);
    } else {
      await createGenre(payload);
    }
    formVisible.value = false;
    await loadGenres(0);
  } catch (error) {
    formError.value = formatApiError(error);
  } finally {
    formLoading.value = false;
  }
}

function openDelete(genre) {
  deleteTarget.value = genre;
  deleteMessage.value = `Delete genre '${genre.name}'?`;
  deleteVisible.value = true;
}

async function confirmDelete() {
  if (!deleteTarget.value) {
    return;
  }
  deleteLoading.value = true;
  errorMessage.value = '';
  try {
    await deleteGenre(deleteTarget.value.id);
    deleteVisible.value = false;
    await loadGenres(0);
  } catch (error) {
    errorMessage.value = formatApiError(error);
  } finally {
    deleteLoading.value = false;
  }
}
</script>
