function normalizeFilterValue(value) {
  if (Array.isArray(value)) {
    return normalizeFilterValue(value[0]);
  }
  return `${value || ''}`.trim();
}

export function readMovieFiltersFromQuery(query = {}) {
  return {
    title: normalizeFilterValue(query.title),
    directorLastName: normalizeFilterValue(query.directorLastName),
    genreName: normalizeFilterValue(query.genreName),
    studioTitle: normalizeFilterValue(query.studioTitle)
  };
}

export function buildMovieFiltersQuery(filters = {}) {
  const normalizedFilters = readMovieFiltersFromQuery(filters);
  const query = {};

  Object.entries(normalizedFilters).forEach(([key, value]) => {
    if (value) {
      query[key] = value;
    }
  });

  return query;
}

export function moviesRouteLocation(filters = {}) {
  return {
    path: '/movies',
    query: buildMovieFiltersQuery(filters)
  };
}

export function directorMovieFilters(source = {}) {
  return {
    directorLastName: source.directorLastName || source.lastName || ''
  };
}

export function genreMovieFilters(source = {}) {
  return {
    genreName: source.genreName || source.name || ''
  };
}

export function studioMovieFilters(source = {}) {
  return {
    studioTitle: source.studioTitle || source.title || ''
  };
}
