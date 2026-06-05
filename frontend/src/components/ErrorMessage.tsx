interface Props {
  error: Error | null | unknown;
  title?: string;
}

export function ErrorMessage({ error, title = 'Error' }: Props) {
  const message =
    error instanceof Error ? error.message : typeof error === 'string' ? error : 'An unexpected error occurred.';

  return (
    <div className="error-box" role="alert">
      <strong>{title}:</strong> {message}
    </div>
  );
}
