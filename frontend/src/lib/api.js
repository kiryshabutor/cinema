const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');

function normalizePath(path) {
  if (!path.startsWith('/')) {
    return `/${path}`;
  }
  return path;
}

export function toApiUrl(path) {
  if (!path) {
    return '';
  }
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  const normalizedPath = normalizePath(path);
  return `${API_BASE_URL}${normalizedPath}`;
}

function buildQuery(params) {
  const searchParams = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && `${value}`.trim() !== '') {
      searchParams.append(key, value);
    }
  });
  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

function parseApiError(status, data) {
  const error = new Error(data?.message || `Request failed with status ${status}`);
  error.status = status;
  error.payload = data;
  error.validation = data?.errors || null;
  return error;
}

async function request(path, options = {}) {
  const {
    method = 'GET',
    body,
    headers = {},
    isFormData = false
  } = options;

  const fetchOptions = {
    method,
    headers: { ...headers }
  };

  if (body !== undefined) {
    if (isFormData) {
      fetchOptions.body = body;
    } else {
      fetchOptions.headers['Content-Type'] = 'application/json';
      fetchOptions.body = JSON.stringify(body);
    }
  }

  const response = await fetch(toApiUrl(path), fetchOptions);
  if (response.status === 204) {
    return null;
  }

  const responseText = await response.text();
  let responseData = null;
  if (responseText) {
    try {
      responseData = JSON.parse(responseText);
    } catch {
      responseData = { message: responseText };
    }
  }

  if (!response.ok) {
    throw parseApiError(response.status, responseData || {});
  }

  return responseData;
}

export function formatApiError(error) {
  if (!error) {
    return 'Unknown error';
  }
  if (error.validation) {
    const firstValidationMessage = Object.values(error.validation)[0];
    if (firstValidationMessage) {
      return `${error.message}: ${firstValidationMessage}`;
    }
  }
  return error.message || 'Request failed';
}

export async function getMoviesSearch(params) {
  return request(`/api/movies/search${buildQuery(params)}`);
}

export async function getMovieById(movieId) {
  return request(`/api/movies/${movieId}`);
}

export async function createMovie(payload) {
  return request('/api/movies', { method: 'POST', body: payload });
}

export async function updateMovie(movieId, payload) {
  return request(`/api/movies/${movieId}`, { method: 'PUT', body: payload });
}

export async function deleteMovie(movieId) {
  return request(`/api/movies/${movieId}`, { method: 'DELETE' });
}

export async function uploadMoviePoster(movieId, file) {
  const formData = new FormData();
  formData.append('file', file);
  return request(`/api/movies/${movieId}/poster`, {
    method: 'POST',
    body: formData,
    isFormData: true
  });
}

export async function searchTmdbPosters(query, year) {
  return request(`/api/posters/tmdb/search${buildQuery({ query, year })}`);
}

export async function importMoviePoster(movieId, posterPath) {
  return request(`/api/movies/${movieId}/poster/import`, {
    method: 'POST',
    body: { posterPath }
  });
}

export async function scrapeMoviePoster(movieId, query, year) {
  return request(`/api/movies/${movieId}/poster/scrape`, {
    method: 'POST',
    body: { query, year }
  });
}

export async function getDirectors(params) {
  return request(`/api/directors${buildQuery(params)}`);
}

export async function createDirector(payload) {
  return request('/api/directors', { method: 'POST', body: payload });
}

export async function updateDirector(id, payload) {
  return request(`/api/directors/${id}`, { method: 'PUT', body: payload });
}

export async function deleteDirector(id) {
  return request(`/api/directors/${id}`, { method: 'DELETE' });
}

export async function getGenres(params) {
  return request(`/api/genres${buildQuery(params)}`);
}

export async function createGenre(payload) {
  return request('/api/genres', { method: 'POST', body: payload });
}

export async function updateGenre(id, payload) {
  return request(`/api/genres/${id}`, { method: 'PUT', body: payload });
}

export async function deleteGenre(id) {
  return request(`/api/genres/${id}`, { method: 'DELETE' });
}

export async function getStudios(params) {
  return request(`/api/studios${buildQuery(params)}`);
}

export async function createStudio(payload) {
  return request('/api/studios', { method: 'POST', body: payload });
}

export async function updateStudio(id, payload) {
  return request(`/api/studios/${id}`, { method: 'PUT', body: payload });
}

export async function deleteStudio(id) {
  return request(`/api/studios/${id}`, { method: 'DELETE' });
}

export async function getMovieReviews(movieId, params) {
  return request(`/api/reviews/movie/${movieId}${buildQuery(params)}`);
}

export async function createReview(payload) {
  return request('/api/reviews', { method: 'POST', body: payload });
}
