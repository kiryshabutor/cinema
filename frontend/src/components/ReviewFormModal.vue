<template>
  <BaseModal :model-value="modelValue" title="Add review" width="520px" @update:model-value="$emit('update:modelValue', $event)">
    <form @submit.prevent="submit">
      <div class="field">
        <label for="review-author">Author alias</label>
        <SuggestInput
          id="review-author"
          v-model="form.authorAlias"
          :suggestions="authorSuggestions"
          maxlength="120"
          required
        />
      </div>

      <div class="field" style="margin-top: 10px">
        <label for="review-rating">Rating (1..10)</label>
        <SuggestInput
          id="review-rating"
          v-model="ratingInput"
          :suggestions="ratingSuggestions"
          placeholder="8"
          required
        />
      </div>

      <div class="field" style="margin-top: 10px">
        <label for="review-comment">Comment</label>
        <textarea id="review-comment" v-model.trim="form.comment" maxlength="1000" />
      </div>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <div class="form-row" style="justify-content: flex-end; margin-top: 12px">
        <button class="btn-outline" type="button" @click="$emit('update:modelValue', false)">Cancel</button>
        <button class="btn-primary" type="submit" :disabled="loading">
          {{ loading ? 'Saving...' : 'Create review' }}
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
  authorSuggestions: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  errorMessage: { type: String, default: '' }
});

const emit = defineEmits(['update:modelValue', 'submit']);

const form = reactive({
  authorAlias: '',
  rating: 8,
  comment: ''
});

const ratingSuggestions = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];
const ratingInput = computed({
  get: () => `${form.rating ?? ''}`,
  set: (value) => {
    const parsed = Number.parseInt(`${value || ''}`.trim(), 10);
    if (Number.isFinite(parsed)) {
      form.rating = parsed;
    }
  }
});

watch(
  () => props.modelValue,
  (opened) => {
    if (!opened) {
      return;
    }
    form.authorAlias = '';
    form.rating = 8;
    form.comment = '';
  },
  { immediate: true }
);

function submit() {
  emit('submit', {
    authorAlias: form.authorAlias.trim(),
    rating: Number(form.rating),
    comment: form.comment || null
  });
}
</script>
