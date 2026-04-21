<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand-block">
        <RouterLink class="brand-link" to="/movies">MovieCat</RouterLink>
      </div>
      <nav class="nav-links">
        <RouterLink to="/movies">Movies</RouterLink>
        <RouterLink to="/directors">Directors</RouterLink>
        <RouterLink to="/genres">Genres</RouterLink>
        <RouterLink to="/studios">Studios</RouterLink>
      </nav>
    </header>

    <main class="page-container">
      <RouterView v-slot="{ Component, route }">
        <Transition name="page-fade" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router';
</script>
