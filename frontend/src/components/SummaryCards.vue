<template>
  <div class="summary-grid">
    <article v-for="item in items" :key="item.label" class="summary-card">
      <p class="summary-card__label">{{ item.label }}</p>
      <strong class="summary-card__value">{{ item.value }}</strong>
      <p class="summary-card__hint">{{ item.hint }}</p>
    </article>
  </div>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    required: true
  }
});
</script>
