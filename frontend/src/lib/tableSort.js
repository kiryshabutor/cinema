export function toggleSortState(paging, field) {
  if (paging.sort === field) {
    paging.direction = paging.direction === 'asc' ? 'desc' : 'asc';
    return;
  }

  paging.sort = field;
  paging.direction = 'asc';
}

export function sortIndicator(paging, field) {
  if (paging.sort !== field) {
    return '↕';
  }
  return paging.direction === 'asc' ? '↑' : '↓';
}

export function sortLabel(sort, labels = {}) {
  return labels[sort] || sort;
}
