<template>
  <div class="pagination">
    <div class="pagination-status">
      <strong>Page {{ page + 1 }}</strong>
      <span> / {{ totalPages || 1 }}</span>
      <span v-if="totalElements !== null"> • Total: {{ totalElements }}</span>
    </div>
    <div class="form-row pagination-actions">
      <button class="btn-outline" type="button" :disabled="page <= 0" @click="$emit('change', page - 1)">
        Prev
      </button>
      <button
        class="btn-outline"
        type="button"
        :disabled="totalPages === 0 || page >= totalPages - 1"
        @click="$emit('change', page + 1)"
      >
        Next
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  page: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  totalElements: { type: Number, default: null }
});

defineEmits(['change']);
</script>
