<template>
  <BaseModal
    :model-value="modelValue"
    :title="`Reviews: ${movieTitle}`"
    width="760px"
    @update:model-value="closeModal"
  >
    <div class="form-row" style="justify-content: space-between; align-items: center; margin-bottom: 10px">
      <div style="color: var(--ink-muted)">
        {{ loading ? 'Loading reviews...' : `Total reviews: ${paging.totalElements}` }}
      </div>
      <button class="btn-primary" type="button" @click="openReviewCreate">Add review</button>
    </div>

    <div class="reviews-list">
      <article v-for="review in reviews" :key="review.id" class="review-item">
        <strong>{{ review.authorAlias }} • Rating: {{ review.rating }}/10</strong>
        <div>{{ review.comment || 'No comment' }}</div>
      </article>
      <article v-if="!loading && reviews.length === 0" class="review-item">No reviews yet.</article>
    </div>

    <PaginationBar
      :page="paging.page"
      :total-pages="paging.totalPages"
      :total-elements="paging.totalElements"
      @change="loadReviews"
    />

    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <ReviewFormModal
      v-model="reviewFormVisible"
      :author-suggestions="authorSuggestions"
      :loading="reviewSaving"
      :error-message="reviewError"
      @submit="submitReview"
    />
  </BaseModal>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { createReview, formatApiError, getMovieReviews } from '../lib/api';
import BaseModal from './BaseModal.vue';
import PaginationBar from './PaginationBar.vue';
import ReviewFormModal from './ReviewFormModal.vue';

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  movie: { type: Object, default: null }
});

const emit = defineEmits(['update:modelValue', 'created']);

const reviews = ref([]);
const loading = ref(false);
const errorMessage = ref('');

const reviewFormVisible = ref(false);
const reviewSaving = ref(false);
const reviewError = ref('');

const paging = reactive({
  page: 0,
  size: 10,
  totalPages: 0,
  totalElements: 0
});

const movieTitle = computed(() => props.movie?.title || 'Movie');
const authorSuggestions = computed(() => {
  const normalizedToOriginal = new Map();
  reviews.value.forEach((review) => {
    const normalized = `${review?.authorAlias || ''}`.trim();
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
});

watch(
  () => [props.modelValue, props.movie?.id],
  async ([opened, movieId]) => {
    if (!opened || !movieId) {
      return;
    }
    await loadReviews(0);
  },
  { immediate: true }
);

function closeModal(value) {
  if (!value) {
    reviewFormVisible.value = false;
    reviewError.value = '';
  }
  emit('update:modelValue', value);
}

function openReviewCreate() {
  reviewError.value = '';
  reviewFormVisible.value = true;
}

async function loadReviews(targetPage = paging.page) {
  if (!props.movie?.id) {
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    const page = await getMovieReviews(props.movie.id, {
      page: targetPage,
      size: paging.size,
      sort: 'id',
      direction: 'desc'
    });

    reviews.value = page.content || [];
    paging.page = page.number;
    paging.totalPages = page.totalPages;
    paging.totalElements = page.totalElements;
  } catch (error) {
    errorMessage.value = formatApiError(error);
  } finally {
    loading.value = false;
  }
}

async function submitReview(payload) {
  if (!props.movie?.id) {
    return;
  }

  reviewSaving.value = true;
  reviewError.value = '';
  try {
    await createReview({
      ...payload,
      movieId: props.movie.id
    });
    reviewFormVisible.value = false;
    await loadReviews(0);
    emit('created');
  } catch (error) {
    reviewError.value = formatApiError(error);
  } finally {
    reviewSaving.value = false;
  }
}
</script>
