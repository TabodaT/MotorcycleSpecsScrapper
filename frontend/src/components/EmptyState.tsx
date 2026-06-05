interface Props {
  message?: string;
  icon?: string;
}

export function EmptyState({ message = 'No data found.', icon = '📭' }: Props) {
  return (
    <div className="empty-state">
      <span className="empty-icon">{icon}</span>
      <p>{message}</p>
    </div>
  );
}
