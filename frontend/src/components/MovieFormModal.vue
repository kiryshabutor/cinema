<template>
  <BaseModal
    :model-value="modelValue"
    :title="movie ? 'Edit movie' : 'Create movie'"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <form @submit.prevent="submit">
      <div class="form-grid">
        <div class="field">
          <label for="movie-title">Title</label>
          <SuggestInput
            id="movie-title"
            v-model="form.title"
            :suggestions="titleSuggestions"
            required
            maxlength="255"
          />
        </div>

        <div class="field">
          <label for="movie-year">Year</label>
          <SuggestInput
            id="movie-year"
            v-model="yearInput"
            :suggestions="yearSuggestions"
            placeholder="2014"
            required
          />
        </div>

        <div class="field">
          <label for="movie-duration">Duration (min)</label>
          <SuggestInput
            id="movie-duration"
            v-model="durationInput"
            :suggestions="durationSuggestions"
            placeholder="120"
            required
          />
        </div>

        <div class="field">
          <label for="movie-view-count">View count</label>
          <SuggestInput
            id="movie-view-count"
            v-model="viewCountInput"
            :suggestions="viewCountSuggestions"
            placeholder="0"
            required
          />
        </div>
      </div>

      <div class="form-grid" style="margin-top: 10px">
        <div class="field">
          <label for="movie-director">Director</label>
          <SuggestInput
            id="movie-director"
            v-model="directorInput"
            :suggestions="directorSuggestions"
            placeholder="Nolan Christopher"
          />
        </div>

        <div class="field">
          <label for="movie-studio">Studio</label>
          <SuggestInput
            id="movie-studio"
            v-model="studioInput"
            :suggestions="studioSuggestions"
            placeholder="Warner Bros"
          />
        </div>

        <div class="field" style="grid-column: 1 / -1">
          <label for="movie-genres">Genres (Ctrl/Cmd + click for multi-select)</label>
          <select id="movie-genres" v-model="form.genreIds" multiple size="6">
            <option v-for="genre in genres" :key="genre.id" :value="String(genre.id)">
              {{ genre.name }}
            </option>
          </select>
        </div>
      </div>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <div class="form-row" style="justify-content: flex-end; margin-top: 12px">
        <button v-if="movie" class="btn-outline" type="button" @click="$emit('openPoster')">Poster</button>
        <button class="btn-outline" type="button" @click="$emit('update:modelValue', false)">Cancel</button>
        <button class="btn-primary" type="submit" :disabled="loading">
          {{ loading ? 'Saving...' : movie ? 'Save changes' : 'Create movie' }}
        </button>
      </div>
    </form>
  </BaseModal>
</template>

<script setup>
import { computed, reactive, watch } from 'vue';
import BaseModal from './BaseModal.vue';
import SuggestInput from './SuggestInput.vue';

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  movie: { type: Object, default: null },
  directors: { type: Array, default: () => [] },
  genres: { type: Array, default: () => [] },
  studios: { type: Array, default: () => [] },
  titleSuggestions: { type: Array, default: () => [] },
  yearSuggestions: { type: Array, default: () => [] },
  durationSuggestions: { type: Array, default: () => [] },
  viewCountSuggestions: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  errorMessage: { type: String, default: '' }
});

const emit = defineEmits(['update:modelValue', 'submit', 'openPoster']);

const form = reactive({
  title: '',
  year: 2000,
  duration: 90,
  viewCount: 0,
  directorQuery: '',
  studioQuery: '',
  directorId: null,
  studioId: null,
  genreIds: []
});

const directorSuggestions = computed(() =>
  props.directors.map((director) => directorDisplayName(director)).filter(Boolean)
);
const studioSuggestions = computed(() =>
  props.studios.map((studio) => `${studio?.title || ''}`.trim()).filter(Boolean)
);

const yearInput = computed({
  get: () => `${form.year ?? ''}`,
  set: (value) => {
    form.year = sanitizeNumberInput(value, form.year, 2000);
  }
});

const durationInput = computed({
  get: () => `${form.duration ?? ''}`,
  set: (value) => {
    form.duration = sanitizeNumberInput(value, form.duration, 90);
  }
});

const viewCountInput = computed({
  get: () => `${form.viewCount ?? ''}`,
  set: (value) => {
    form.viewCount = sanitizeNumberInput(value, form.viewCount, 0);
  }
});

const directorInput = computed({
  get: () => form.directorQuery,
  set: (value) => {
    const normalized = `${value || ''}`.trim();
    form.directorQuery = normalized;
    if (!normalized) {
      form.directorId = null;
      return;
    }

    const matchedDirector = props.directors.find((director) =>
      directorDisplayName(director).localeCompare(normalized, undefined, { sensitivity: 'base' }) === 0
    );
    form.directorId = matchedDirector ? matchedDirector.id : null;
  }
});

const studioInput = computed({
  get: () => form.studioQuery,
  set: (value) => {
    const normalized = `${value || ''}`.trim();
    form.studioQuery = normalized;
    if (!normalized) {
      form.studioId = null;
      return;
    }

    const matchedStudio = props.studios.find((studio) =>
      `${studio?.title || ''}`.trim().localeCompare(normalized, undefined, { sensitivity: 'base' }) === 0
    );
    form.studioId = matchedStudio ? matchedStudio.id : null;
  }
});

watch(
  () => [props.modelValue, props.movie],
  () => {
    if (!props.modelValue) {
      return;
    }
    if (props.movie) {
      form.title = props.movie.title || '';
      form.year = props.movie.year || 2000;
      form.duration = props.movie.duration || 90;
      form.viewCount = props.movie.viewCount ?? 0;
      form.directorId = props.movie.directorId || null;
      form.studioId = props.movie.studioId || null;
      form.directorQuery = directorDisplayName(props.movie);
      form.studioQuery = `${props.movie.studioTitle || ''}`.trim();
      form.genreIds = (props.movie.genres || []).map((genre) => String(genre.id));
    } else {
      form.title = '';
      form.year = 2000;
      form.duration = 90;
      form.viewCount = 0;
      form.directorQuery = '';
      form.studioQuery = '';
      form.directorId = null;
      form.studioId = null;
      form.genreIds = [];
    }
  },
  { immediate: true }
);

function submit() {
  emit('submit', {
    title: form.title.trim(),
    year: Number(form.year),
    duration: Number(form.duration),
    viewCount: Number(form.viewCount),
    directorId: form.directorId ? Number(form.directorId) : null,
    studioId: form.studioId ? Number(form.studioId) : null,
    genreIds: form.genreIds.map((item) => Number(item))
  });
}

function sanitizeNumberInput(value, currentValue, fallbackValue) {
  const normalized = `${value || ''}`.trim();
  if (!normalized) {
    return fallbackValue;
  }

  const parsed = Number.parseInt(normalized, 10);
  return Number.isFinite(parsed) ? parsed : currentValue;
}

function directorDisplayName(director) {
  if (!director) {
    return '';
  }
  return [director.lastName, director.firstName, director.middleName].filter(Boolean).join(' ').trim();
}
</script>
