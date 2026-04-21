<template>
  <BaseModal :model-value="modelValue" :title="`Poster tools: ${movieTitle}`" @update:model-value="closeModal">
    <section>
      <h4>Upload local image</h4>
      <div class="form-row">
        <input type="file" accept=".jpg,.jpeg,.png,.webp,.gif" @change="onFileSelected" />
        <button class="btn-primary" type="button" :disabled="!selectedFile || loadingUpload" @click="uploadSelectedFile">
          {{ loadingUpload ? 'Uploading...' : 'Upload' }}
        </button>
      </div>
    </section>

    <section style="margin-top: 14px">
      <h4>Find poster via Wikipedia API and import</h4>
      <div class="form-row">
        <input v-model.trim="wikiQuery" placeholder="Movie title" style="flex: 1 1 280px" />
        <input v-model.number="wikiYear" type="number" placeholder="Year" min="1888" max="2100" style="width: 110px" />
        <button class="btn-outline" type="button" :disabled="loadingScrape" @click="scrapeFromWikipedia">
          {{ loadingScrape ? 'Searching...' : 'Find & import' }}
        </button>
      </div>
      <p style="color: var(--ink-muted); margin: 8px 0 0">
        No API key required. The backend queries Wikipedia MediaWiki API and saves the found poster.
      </p>
    </section>

    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>
    <div v-if="successMessage" class="success-banner">{{ successMessage }}</div>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue';
import BaseModal from './BaseModal.vue';
import { formatApiError, scrapeMoviePoster, uploadMoviePoster } from '../lib/api';

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  movieId: { type: Number, required: true },
  movieTitle: { type: String, default: '' }
});

const emit = defineEmits(['update:modelValue', 'updated']);

const selectedFile = ref(null);
const wikiQuery = ref('');
const wikiYear = ref('');
const loadingUpload = ref(false);
const loadingScrape = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

watch(
  () => props.modelValue,
  (opened) => {
    if (!opened) {
      return;
    }
    selectedFile.value = null;
    errorMessage.value = '';
    successMessage.value = '';
    wikiQuery.value = props.movieTitle || '';
    wikiYear.value = '';
  },
  { immediate: true }
);

function closeModal(value) {
  emit('update:modelValue', value);
}

function onFileSelected(event) {
  const file = event.target?.files?.[0] || null;
  selectedFile.value = file;
}

async function uploadSelectedFile() {
  if (!selectedFile.value) {
    return;
  }
  loadingUpload.value = true;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    const response = await uploadMoviePoster(props.movieId, selectedFile.value);
    successMessage.value = `Poster uploaded: ${response.url}`;
    emit('updated');
  } catch (error) {
    errorMessage.value = formatApiError(error);
  } finally {
    loadingUpload.value = false;
  }
}

async function scrapeFromWikipedia() {
  loadingScrape.value = true;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    const response = await scrapeMoviePoster(props.movieId, wikiQuery.value, wikiYear.value || null);
    successMessage.value = `Poster imported from Wikipedia: ${response.url}`;
    emit('updated');
  } catch (error) {
    errorMessage.value = formatApiError(error);
  } finally {
    loadingScrape.value = false;
  }
}
</script>
