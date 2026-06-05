interface Props {
  size?: 'sm' | 'md' | 'lg';
}

export function LoadingSpinner({ size = 'md' }: Props) {
  return (
    <div className={`spinner spinner-${size}`} role="status" aria-label="Loading">
      <span className="sr-only">Loading…</span>
    </div>
  );
}

export function LoadingBlock() {
  return (
    <div className="loading-block">
      <LoadingSpinner size="lg" />
      <p className="loading-text">Loading…</p>
    </div>
  );
}
