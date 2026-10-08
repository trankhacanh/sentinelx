export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-center gap-2 pt-4">
      <button
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
        className="rounded border border-surface-border px-3 py-1 text-sm text-gray-300 disabled:opacity-40"
      >
        Previous
      </button>
      <span className="text-sm text-gray-400">
        Page {page + 1} of {totalPages}
      </span>
      <button
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
        className="rounded border border-surface-border px-3 py-1 text-sm text-gray-300 disabled:opacity-40"
      >
        Next
      </button>
    </div>
  );
}