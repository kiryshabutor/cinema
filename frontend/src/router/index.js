import { createRouter, createWebHashHistory } from 'vue-router';
import MoviesPage from '../pages/MoviesPage.vue';
import MovieDetailsPage from '../pages/MovieDetailsPage.vue';
import DirectorsPage from '../pages/DirectorsPage.vue';
import GenresPage from '../pages/GenresPage.vue';
import StudiosPage from '../pages/StudiosPage.vue';

const router = createRouter({
  history: createWebHashHistory('/'),
  routes: [
    { path: '/', redirect: '/movies' },
    { path: '/movies', component: MoviesPage },
    { path: '/movies/:id', component: MovieDetailsPage, props: true },
    { path: '/directors', component: DirectorsPage },
    { path: '/genres', component: GenresPage },
    { path: '/studios', component: StudiosPage }
  ]
});

export default router;
