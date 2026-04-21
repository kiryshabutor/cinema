<template>
  <div class="suggest-input">
    <input
      :id="id"
      :value="modelValue"
      :placeholder="placeholder"
      :maxlength="maxlength || undefined"
      :required="required"
      :disabled="disabled"
      autocomplete="off"
      @focus="onFocus"
      @blur="onBlur"
      @input="onInput"
      @keydown.down.prevent="highlightNext"
      @keydown.up.prevent="highlightPrev"
      @keydown.enter.prevent="selectHighlighted"
      @keydown.escape.prevent="closeDropdown"
    />

    <div v-if="showDropdown" class="suggest-menu">
      <button
        v-for="(option, index) in visibleSuggestions"
        :key="`${option}-${index}`"
        class="suggest-option"
        type="button"
        :class="{ active: index === highlightedIndex }"
        @mousedown.prevent="selectOption(option)"
      >
        {{ option }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';

const props = defineProps({
  modelValue: { type: String, default: '' },
  suggestions: { type: Array, default: () => [] },
  id: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  maxlength: { type: Number, default: null },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  minChars: { type: Number, default: 1 },
  maxItems: { type: Number, default: 8 }
});

const emit = defineEmits(['update:modelValue', 'blur']);

const isFocused = ref(false);
const highlightedIndex = ref(-1);

const normalizedQuery = computed(() => `${props.modelValue || ''}`.trim().toLocaleLowerCase());

const filteredSuggestions = computed(() => {
  const query = normalizedQuery.value;
  if (query.length < props.minChars) {
    return [];
  }

  const deduped = Array.from(new Set((props.suggestions || []).map((item) => `${item || ''}`.trim()).filter(Boolean)));
  const matched = deduped.filter((item) => item.toLocaleLowerCase().includes(query));

  matched.sort((left, right) => {
    const leftLower = left.toLocaleLowerCase();
    const rightLower = right.toLocaleLowerCase();
    const leftStarts = leftLower.startsWith(query) ? 0 : 1;
    const rightStarts = rightLower.startsWith(query) ? 0 : 1;
    if (leftStarts !== rightStarts) {
      return leftStarts - rightStarts;
    }
    return left.localeCompare(right, undefined, { sensitivity: 'base' });
  });

  return matched;
});

const visibleSuggestions = computed(() =>
  filteredSuggestions.value.slice(0, Math.max(1, props.maxItems))
);

const showDropdown = computed(() =>
  isFocused.value && visibleSuggestions.value.length > 0 && normalizedQuery.value.length >= props.minChars
);

watch(showDropdown, (opened) => {
  if (!opened) {
    highlightedIndex.value = -1;
  }
});

function onFocus() {
  isFocused.value = true;
}

function onBlur() {
  setTimeout(() => {
    isFocused.value = false;
    emit('blur');
  }, 80);
}

function onInput(event) {
  emit('update:modelValue', event.target.value);
}

function selectOption(option) {
  emit('update:modelValue', option);
  isFocused.value = false;
  highlightedIndex.value = -1;
}

function highlightNext() {
  if (!showDropdown.value) {
    return;
  }
  highlightedIndex.value = (highlightedIndex.value + 1) % visibleSuggestions.value.length;
}

function highlightPrev() {
  if (!showDropdown.value) {
    return;
  }
  if (highlightedIndex.value <= 0) {
    highlightedIndex.value = visibleSuggestions.value.length - 1;
    return;
  }
  highlightedIndex.value -= 1;
}

function selectHighlighted() {
  if (!showDropdown.value) {
    return;
  }
  if (highlightedIndex.value < 0 || highlightedIndex.value >= visibleSuggestions.value.length) {
    selectOption(visibleSuggestions.value[0]);
    return;
  }
  selectOption(visibleSuggestions.value[highlightedIndex.value]);
}

function closeDropdown() {
  isFocused.value = false;
  highlightedIndex.value = -1;
}
</script>
