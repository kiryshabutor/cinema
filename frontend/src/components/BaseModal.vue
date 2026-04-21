<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-backdrop" @click.self="close">
      <div class="modal-card" :style="{ width: width || undefined }">
        <header class="modal-head">
          <h3>{{ title }}</h3>
          <button class="btn-outline" type="button" @click="close">Close</button>
        </header>
        <div class="modal-body">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { onBeforeUnmount, watch } from 'vue';

let openModalCount = 0;

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  width: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:modelValue']);
let isLockedByThisInstance = false;

watch(
  () => props.modelValue,
  (isOpen) => {
    if (typeof document === 'undefined') {
      return;
    }

    if (isOpen && !isLockedByThisInstance) {
      openModalCount += 1;
      isLockedByThisInstance = true;
    } else if (!isOpen && isLockedByThisInstance) {
      openModalCount = Math.max(0, openModalCount - 1);
      isLockedByThisInstance = false;
    }

    document.body.classList.toggle('modal-open', openModalCount > 0);
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  if (!isLockedByThisInstance || typeof document === 'undefined') {
    return;
  }
  openModalCount = Math.max(0, openModalCount - 1);
  isLockedByThisInstance = false;
  document.body.classList.toggle('modal-open', openModalCount > 0);
});

function close() {
  emit('update:modelValue', false);
}
</script>
