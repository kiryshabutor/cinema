<template>
  <BaseModal
    :model-value="modelValue"
    :title="director ? 'Edit director' : 'Create director'"
    width="560px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <form @submit.prevent="submit">
      <div class="field">
        <label for="director-last-name">Last name</label>
        <SuggestInput
          id="director-last-name"
          v-model="form.lastName"
          :suggestions="lastNameSuggestions"
          maxlength="120"
          required
        />
      </div>

      <div class="field" style="margin-top: 10px">
        <label for="director-first-name">First name</label>
        <SuggestInput
          id="director-first-name"
          v-model="form.firstName"
          :suggestions="firstNameSuggestions"
          maxlength="120"
          required
        />
      </div>

      <div class="field" style="margin-top: 10px">
        <label for="director-middle-name">Middle name</label>
        <SuggestInput
          id="director-middle-name"
          v-model="form.middleName"
          :suggestions="middleNameSuggestions"
          maxlength="120"
        />
      </div>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <div class="form-row" style="justify-content: flex-end; margin-top: 12px">
        <button class="btn-outline" type="button" @click="$emit('update:modelValue', false)">Cancel</button>
        <button class="btn-primary" type="submit" :disabled="loading">
          {{ loading ? 'Saving...' : director ? 'Save changes' : 'Create director' }}
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
  director: { type: Object, default: null },
  existingDirectors: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  errorMessage: { type: String, default: '' }
});

const emit = defineEmits(['update:modelValue', 'submit']);

const form = reactive({
  lastName: '',
  firstName: '',
  middleName: ''
});

const lastNameSuggestions = computed(() =>
  uniqueSuggestions(props.existingDirectors.map((item) => item.lastName))
);
const firstNameSuggestions = computed(() =>
  uniqueSuggestions(props.existingDirectors.map((item) => item.firstName))
);
const middleNameSuggestions = computed(() =>
  uniqueSuggestions(props.existingDirectors.map((item) => item.middleName))
);

watch(
  () => [props.modelValue, props.director],
  () => {
    if (!props.modelValue) {
      return;
    }
    if (props.director) {
      form.lastName = props.director.lastName || '';
      form.firstName = props.director.firstName || '';
      form.middleName = props.director.middleName || '';
    } else {
      form.lastName = '';
      form.firstName = '';
      form.middleName = '';
    }
  },
  { immediate: true }
);

function submit() {
  emit('submit', {
    lastName: form.lastName.trim(),
    firstName: form.firstName.trim(),
    middleName: form.middleName.trim() || null
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
