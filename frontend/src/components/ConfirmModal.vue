<template>
  <BaseModal :model-value="modelValue" :title="title" width="460px" @update:model-value="$emit('update:modelValue', $event)">
    <p>{{ message }}</p>
    <div class="form-row" style="justify-content: flex-end; margin-top: 10px">
      <button class="btn-outline" type="button" @click="$emit('update:modelValue', false)">Cancel</button>
      <button class="btn-danger" type="button" :disabled="loading" @click="$emit('confirm')">
        {{ loading ? 'Deleting...' : 'Delete' }}
      </button>
    </div>
  </BaseModal>
</template>

<script setup>
import BaseModal from './BaseModal.vue';

defineProps({
  modelValue: { type: Boolean, required: true },
  title: { type: String, default: 'Confirm action' },
  message: { type: String, required: true },
  loading: { type: Boolean, default: false }
});

defineEmits(['update:modelValue', 'confirm']);
</script>
