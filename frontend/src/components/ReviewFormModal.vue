<template>
  <BaseModal :model-value="modelValue" title="Add review" width="520px" @update:model-value="$emit('update:modelValue', $event)">
    <form @submit.prevent="submit">
      <div class="field">
        <label for="review-author">Author alias</label>
        <input id="review-author" v-model.trim="form.authorAlias" maxlength="120" required />
      </div>

      <div class="field" style="margin-top: 10px">
        <label for="review-rating">Rating (1..10)</label>
        <input id="review-rating" v-model.number="form.rating" type="number" min="1" max="10" required />
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
import { reactive, watch } from 'vue';
import BaseModal from './BaseModal.vue';

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  loading: { type: Boolean, default: false },
  errorMessage: { type: String, default: '' }
});

const emit = defineEmits(['update:modelValue', 'submit']);

const form = reactive({
  authorAlias: '',
  rating: 8,
  comment: ''
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
    authorAlias: form.authorAlias,
    rating: Number(form.rating),
    comment: form.comment || null
  });
}
</script>
