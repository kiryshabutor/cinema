<template>
  <BaseModal
    :model-value="modelValue"
    :title="genre ? 'Edit genre' : 'Create genre'"
    width="480px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <form @submit.prevent="submit">
      <div class="field">
        <label for="genre-name">Genre name</label>
        <SuggestInput
          id="genre-name"
          v-model="form.name"
          :suggestions="nameSuggestions"
          maxlength="100"
          required
        />
      </div>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <div class="form-row" style="justify-content: flex-end; margin-top: 12px">
        <button class="btn-outline" type="button" @click="$emit('update:modelValue', false)">Cancel</button>
        <button class="btn-primary" type="submit" :disabled="loading">
          {{ loading ? 'Saving...' : genre ? 'Save changes' : 'Create genre' }}
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
  genre: { type: Object, default: null },
  existingGenres: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  errorMessage: { type: String, default: '' }
});

const emit = defineEmits(['update:modelValue', 'submit']);

const form = reactive({
  name: ''
});

const nameSuggestions = computed(() =>
  uniqueSuggestions(props.existingGenres.map((item) => item.name))
);

watch(
  () => [props.modelValue, props.genre],
  () => {
    if (!props.modelValue) {
      return;
    }
    form.name = props.genre?.name || '';
  },
  { immediate: true }
);

function submit() {
  emit('submit', { name: form.name.trim() });
}

function uniqueSuggestions(values) {
  const normalizedToOriginal = new Map();
  values.forEach((value) => {
    const normalized = `${value || ''}`.trim();
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
}
</script>
