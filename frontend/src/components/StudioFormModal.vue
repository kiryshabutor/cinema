<template>
  <BaseModal
    :model-value="modelValue"
    :title="studio ? 'Edit studio' : 'Create studio'"
    width="560px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <form @submit.prevent="submit">
      <div class="field">
        <label for="studio-title">Title</label>
        <SuggestInput
          id="studio-title"
          v-model="form.title"
          :suggestions="titleSuggestions"
          maxlength="255"
          required
        />
      </div>

      <div class="field" style="margin-top: 10px">
        <label for="studio-address">Address</label>
        <SuggestInput
          id="studio-address"
          v-model="form.address"
          :suggestions="addressSuggestions"
          maxlength="255"
        />
      </div>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <div class="form-row" style="justify-content: flex-end; margin-top: 12px">
        <button class="btn-outline" type="button" @click="$emit('update:modelValue', false)">Cancel</button>
        <button class="btn-primary" type="submit" :disabled="loading">
          {{ loading ? 'Saving...' : studio ? 'Save changes' : 'Create studio' }}
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
  studio: { type: Object, default: null },
  existingStudios: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  errorMessage: { type: String, default: '' }
});

const emit = defineEmits(['update:modelValue', 'submit']);

const form = reactive({
  title: '',
  address: ''
});

const titleSuggestions = computed(() =>
  uniqueSuggestions(props.existingStudios.map((item) => item.title))
);
const addressSuggestions = computed(() =>
  uniqueSuggestions(props.existingStudios.map((item) => item.address))
);

watch(
  () => [props.modelValue, props.studio],
  () => {
    if (!props.modelValue) {
      return;
    }
    if (props.studio) {
      form.title = props.studio.title || '';
      form.address = props.studio.address || '';
    } else {
      form.title = '';
      form.address = '';
    }
  },
  { immediate: true }
);

function submit() {
  emit('submit', {
    title: form.title.trim(),
    address: form.address.trim() || null
  });
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
