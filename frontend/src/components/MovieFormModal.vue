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
          <input id="movie-year" v-model.number="form.year" type="number" min="1888" max="2027" required />
        </div>

        <div class="field">
          <label for="movie-duration">Duration (min)</label>
          <input id="movie-duration" v-model.number="form.duration" type="number" min="1" required />
        </div>

        <div class="field">
          <label for="movie-view-count">View count</label>
          <input id="movie-view-count" v-model.number="form.viewCount" type="number" min="0" required />
        </div>
      </div>

      <div class="form-grid" style="margin-top: 10px">
        <div class="field">
          <label for="movie-director">Director</label>
          <select id="movie-director" v-model="form.directorId">
            <option value="">No director</option>
            <option v-for="director in directors" :key="director.id" :value="String(director.id)">
              {{ director.lastName }} {{ director.firstName }} {{ director.middleName || '' }}
            </option>
          </select>
        </div>

        <div class="field">
          <label for="movie-studio">Studio</label>
          <select id="movie-studio" v-model="form.studioId">
            <option value="">No studio</option>
            <option v-for="studio in studios" :key="studio.id" :value="String(studio.id)">
              {{ studio.title }}
            </option>
          </select>
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
import { reactive, watch } from 'vue';
import BaseModal from './BaseModal.vue';
import SuggestInput from './SuggestInput.vue';

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  movie: { type: Object, default: null },
  directors: { type: Array, default: () => [] },
  genres: { type: Array, default: () => [] },
  studios: { type: Array, default: () => [] },
  titleSuggestions: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  errorMessage: { type: String, default: '' }
});

const emit = defineEmits(['update:modelValue', 'submit', 'openPoster']);

const form = reactive({
  title: '',
  year: 2000,
  duration: 90,
  viewCount: 0,
  directorId: '',
  studioId: '',
  genreIds: []
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
      form.directorId = props.movie.directorId ? String(props.movie.directorId) : '';
      form.studioId = props.movie.studioId ? String(props.movie.studioId) : '';
      form.genreIds = (props.movie.genres || []).map((genre) => String(genre.id));
    } else {
      form.title = '';
      form.year = 2000;
      form.duration = 90;
      form.viewCount = 0;
      form.directorId = '';
      form.studioId = '';
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
</script>
