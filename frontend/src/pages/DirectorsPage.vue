<template>
  <section>
    <div class="page-head">
      <div>
        <h2>Directors</h2>
      </div>
      <button class="btn-primary" type="button" @click="openCreate">Create director</button>
    </div>

    <div class="list-toolbar">
      <div class="list-toolbar-summary">
        <span class="list-toolbar-label">Sort by</span>
        <strong>{{ currentSortLabel }}</strong>
      </div>

      <div class="field list-toolbar-field">
        <label for="directors-direction">Direction</label>
        <SuggestInput
          id="directors-direction"
          v-model="directionInput"
          :suggestions="directionSuggestions"
          placeholder="asc"
          @blur="applyTableSettings"
        />
      </div>

      <div class="field list-toolbar-field">
        <label for="directors-size">Page size</label>
        <SuggestInput
          id="directors-size"
          v-model="sizeInput"
          :suggestions="sizeSuggestions"
          placeholder="10"
          @blur="applyTableSettings"
        />
      </div>
    </div>

    <div class="table-wrap">
      <table class="table table--entity">
        <colgroup>
          <col class="table-col-id" />
          <col class="table-col-name" />
          <col class="table-col-name" />
          <col class="table-col-name" />
          <col class="table-col-actions" />
        </colgroup>
        <thead>
          <tr>
            <th class="table-head-center">
              <button
                class="table-sort-button"
                :class="{ 'is-active': paging.sort === 'id' }"
                type="button"
                @click="toggleDirectorSort('id')"
              >
                <span>ID</span>
                <span class="table-sort-indicator">{{ directorSortIndicator('id') }}</span>
              </button>
            </th>
            <th>
              <button
                class="table-sort-button"
                :class="{ 'is-active': paging.sort === 'lastName' }"
                type="button"
                @click="toggleDirectorSort('lastName')"
              >
                <span>Last name</span>
                <span class="table-sort-indicator">{{ directorSortIndicator('lastName') }}</span>
              </button>
            </th>
            <th>
              <button
                class="table-sort-button"
                :class="{ 'is-active': paging.sort === 'firstName' }"
                type="button"
                @click="toggleDirectorSort('firstName')"
              >
                <span>First name</span>
                <span class="table-sort-indicator">{{ directorSortIndicator('firstName') }}</span>
              </button>
            </th>
            <th>
              <button
                class="table-sort-button"
                :class="{ 'is-active': paging.sort === 'middleName' }"
                type="button"
                @click="toggleDirectorSort('middleName')"
              >
                <span>Middle name</span>
                <span class="table-sort-indicator">{{ directorSortIndicator('middleName') }}</span>
              </button>
            </th>
            <th class="table-head-center">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="director in directors" :key="director.id">
            <td class="table-cell-center">{{ director.id }}</td>
            <td>
              <RouterLink class="movie-filter-link" :to="directorMoviesLocation(director)">
                {{ director.lastName }}
              </RouterLink>
            </td>
            <td>
              <RouterLink class="movie-filter-link" :to="directorMoviesLocation(director)">
                {{ director.firstName }}
              </RouterLink>
            </td>
            <td>
              <RouterLink v-if="director.middleName" class="movie-filter-link" :to="directorMoviesLocation(director)">
                {{ director.middleName }}
              </RouterLink>
              <span v-else>—</span>
            </td>
            <td class="table-cell-actions">
              <div class="table-actions table-actions--icon">
                <button
                  class="icon-action-button"
                  type="button"
                  title="Edit director"
                  aria-label="Edit director"
                  @click="openEdit(director)"
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
                  title="Delete director"
                  aria-label="Delete director"
                  @click="openDelete(director)"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M6 6 18 18" />
                    <path d="M18 6 6 18" />
                  </svg>
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="directors.length === 0 && !loading">
            <td colspan="5">No directors found.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <PaginationBar
      :page="paging.page"
      :total-pages="paging.totalPages"
      :total-elements="paging.totalElements"
      @change="loadDirectors"
    />

    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <DirectorFormModal
      v-model="formVisible"
      :director="selectedDirector"
      :existing-directors="directors"
      :loading="formLoading"
      :error-message="formError"
      @submit="submitDirector"
    />

    <ConfirmModal
      v-model="deleteVisible"
      title="Delete director"
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
import DirectorFormModal from '../components/DirectorFormModal.vue';
import PaginationBar from '../components/PaginationBar.vue';
import SuggestInput from '../components/SuggestInput.vue';
import { directorMovieFilters, moviesRouteLocation } from '../lib/movieFilters';
import { sortIndicator, sortLabel, toggleSortState } from '../lib/tableSort';
import {
  createDirector,
  deleteDirector,
  formatApiError,
  getDirectors,
  updateDirector
} from '../lib/api';

const directors = ref([]);
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

const directionSuggestions = ['asc', 'desc'];
const sizeSuggestions = ['5', '10', '20'];
const directorSortLabels = {
  id: 'ID',
  lastName: 'Last name',
  firstName: 'First name',
  middleName: 'Middle name'
};
const currentSortLabel = computed(() => sortLabel(paging.sort, directorSortLabels));

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

const formVisible = ref(false);
const formLoading = ref(false);
const formError = ref('');
const selectedDirector = ref(null);

const deleteVisible = ref(false);
const deleteLoading = ref(false);
const deleteTarget = ref(null);
const deleteMessage = ref('');

onMounted(() => {
  loadDirectors(0);
});

function directorMoviesLocation(director) {
  return moviesRouteLocation(directorMovieFilters(director));
}

function applyTableSettings() {
  loadDirectors(0);
}

function toggleDirectorSort(field) {
  toggleSortState(paging, field);
  loadDirectors(0);
}

function directorSortIndicator(field) {
  return sortIndicator(paging, field);
}

async function loadDirectors(targetPage = paging.page) {
  loading.value = true;
  errorMessage.value = '';
  try {
    const page = await getDirectors({
      page: targetPage,
      size: paging.size,
      sort: paging.sort,
      direction: paging.direction
    });
    directors.value = page.content || [];
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
  selectedDirector.value = null;
  formError.value = '';
  formVisible.value = true;
}

function openEdit(director) {
  selectedDirector.value = director;
  formError.value = '';
  formVisible.value = true;
}

async function submitDirector(payload) {
  formLoading.value = true;
  formError.value = '';
  try {
    if (selectedDirector.value) {
      await updateDirector(selectedDirector.value.id, payload);
    } else {
      await createDirector(payload);
    }
    formVisible.value = false;
    await loadDirectors(0);
  } catch (error) {
    formError.value = formatApiError(error);
  } finally {
    formLoading.value = false;
  }
}

function openDelete(director) {
  deleteTarget.value = director;
  deleteMessage.value = `Delete director '${director.lastName} ${director.firstName}'?`;
  deleteVisible.value = true;
}

async function confirmDelete() {
  if (!deleteTarget.value) {
    return;
  }
  deleteLoading.value = true;
  errorMessage.value = '';
  try {
    await deleteDirector(deleteTarget.value.id);
    deleteVisible.value = false;
    await loadDirectors(0);
  } catch (error) {
    errorMessage.value = formatApiError(error);
  } finally {
    deleteLoading.value = false;
  }
}
</script>
