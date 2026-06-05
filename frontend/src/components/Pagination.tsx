import type { Meta } from '../api/types';

interface Props {
  meta: Meta;
  onPageChange: (page: number) => void;
}

export function Pagination({ meta, onPageChange }: Props) {
  const totalPages = meta.size > 0 ? Math.ceil(meta.total / meta.size) : 1;
  const currentPage = meta.page;

  if (totalPages <= 1) return null;

  return (
    <div className="pagination">
      <button
        className="btn btn-sm"
        disabled={currentPage <= 0}
        onClick={() => onPageChange(currentPage - 1)}
      >
        ‹ Prev
      </button>

      <span className="pagination-info">
        Page {currentPage + 1} of {totalPages} ({meta.total} total)
      </span>

      <button
        className="btn btn-sm"
        disabled={currentPage >= totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
      >
        Next ›
      </button>
    </div>
  );
}
