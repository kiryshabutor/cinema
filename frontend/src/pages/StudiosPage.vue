<template>
  <section>
    <div class="page-head">
      <div>
        <h2>Studios</h2>
      </div>
      <button class="btn-primary" type="button" @click="openCreate">Create studio</button>
    </div>

    <div class="list-toolbar">
      <div class="list-toolbar-summary">
        <span class="list-toolbar-label">Sort by</span>
        <strong>{{ currentSortLabel }}</strong>
      </div>

      <div class="field list-toolbar-field">
        <label for="studios-direction">Direction</label>
        <SuggestInput
          id="studios-direction"
          v-model="directionInput"
          :suggestions="directionSuggestions"
          placeholder="asc"
          @blur="applyTableSettings"
        />
      </div>

      <div class="field list-toolbar-field">
        <label for="studios-size">Page size</label>
        <SuggestInput
          id="studios-size"
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
          <col class="table-col-title-medium" />
          <col class="table-col-address" />
          <col class="table-col-actions-medium" />
        </colgroup>
        <thead>
          <tr>
            <th class="table-head-center">
              <button
                class="table-sort-button"
                :class="{ 'is-active': paging.sort === 'id' }"
                type="button"
                @click="toggleStudioSort('id')"
              >
                <span>ID</span>
                <span class="table-sort-indicator">{{ studioSortIndicator('id') }}</span>
              </button>
            </th>
            <th>
              <button
                class="table-sort-button"
                :class="{ 'is-active': paging.sort === 'title' }"
                type="button"
                @click="toggleStudioSort('title')"
              >
                <span>Title</span>
                <span class="table-sort-indicator">{{ studioSortIndicator('title') }}</span>
              </button>
            </th>
            <th>
              <button
                class="table-sort-button"
                :class="{ 'is-active': paging.sort === 'address' }"
                type="button"
                @click="toggleStudioSort('address')"
              >
                <span>Address</span>
                <span class="table-sort-indicator">{{ studioSortIndicator('address') }}</span>
              </button>
            </th>
            <th class="table-head-center">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="studio in studios" :key="studio.id">
            <td class="table-cell-center">{{ studio.id }}</td>
            <td>
              <RouterLink class="movie-filter-link" :to="studioMoviesLocation(studio)">
                {{ studio.title }}
              </RouterLink>
            </td>
            <td>{{ studio.address || '—' }}</td>
            <td class="table-cell-actions">
              <div class="table-actions table-actions--icon">
                <button
                  class="icon-action-button"
                  type="button"
                  title="Edit studio"
                  aria-label="Edit studio"
                  @click="openEdit(studio)"
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
                  title="Delete studio"
                  aria-label="Delete studio"
                  @click="openDelete(studio)"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M6 6 18 18" />
                    <path d="M18 6 6 18" />
                  </svg>
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="studios.length === 0 && !loading">
            <td colspan="4">No studios found.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <PaginationBar
      :page="paging.page"
      :total-pages="paging.totalPages"
      :total-elements="paging.totalElements"
      @change="loadStudios"
    />

    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <StudioFormModal
      v-model="formVisible"
      :studio="selectedStudio"
      :existing-studios="studios"
      :loading="formLoading"
      :error-message="formError"
      @submit="submitStudio"
    />

    <ConfirmModal
      v-model="deleteVisible"
      title="Delete studio"
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
import PaginationBar from '../components/PaginationBar.vue';
import SuggestInput from '../components/SuggestInput.vue';
import StudioFormModal from '../components/StudioFormModal.vue';
import { moviesRouteLocation, studioMovieFilters } from '../lib/movieFilters';
import { sortIndicator, sortLabel, toggleSortState } from '../lib/tableSort';
import { createStudio, deleteStudio, formatApiError, getStudios, updateStudio } from '../lib/api';

const studios = ref([]);
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
const studioSortLabels = {
  id: 'ID',
  title: 'Title',
  address: 'Address'
};
const currentSortLabel = computed(() => sortLabel(paging.sort, studioSortLabels));

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
const selectedStudio = ref(null);

const deleteVisible = ref(false);
const deleteLoading = ref(false);
const deleteTarget = ref(null);
const deleteMessage = ref('');

onMounted(() => {
  loadStudios(0);
});

function studioMoviesLocation(studio) {
  return moviesRouteLocation(studioMovieFilters(studio));
}

function applyTableSettings() {
  loadStudios(0);
}

function toggleStudioSort(field) {
  toggleSortState(paging, field);
  loadStudios(0);
}

function studioSortIndicator(field) {
  return sortIndicator(paging, field);
}

async function loadStudios(targetPage = paging.page) {
  loading.value = true;
  errorMessage.value = '';
  try {
    const page = await getStudios({
      page: targetPage,
      size: paging.size,
      sort: paging.sort,
      direction: paging.direction
    });
    studios.value = page.content || [];
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
  selectedStudio.value = null;
  formError.value = '';
  formVisible.value = true;
}

function openEdit(studio) {
  selectedStudio.value = studio;
  formError.value = '';
  formVisible.value = true;
}

async function submitStudio(payload) {
  formLoading.value = true;
  formError.value = '';
  try {
    if (selectedStudio.value) {
      await updateStudio(selectedStudio.value.id, payload);
    } else {
      await createStudio(payload);
    }
    formVisible.value = false;
    await loadStudios(0);
  } catch (error) {
    formError.value = formatApiError(error);
  } finally {
    formLoading.value = false;
  }
}

function openDelete(studio) {
  deleteTarget.value = studio;
  deleteMessage.value = `Delete studio '${studio.title}'?`;
  deleteVisible.value = true;
}

async function confirmDelete() {
  if (!deleteTarget.value) {
    return;
  }
  deleteLoading.value = true;
  errorMessage.value = '';
  try {
    await deleteStudio(deleteTarget.value.id);
    deleteVisible.value = false;
    await loadStudios(0);
  } catch (error) {
    errorMessage.value = formatApiError(error);
  } finally {
    deleteLoading.value = false;
  }
}
</script>
