interface Props {
  amount?: number | null;
  currency?: string | null;
}

export function Money({ amount, currency = 'EUR' }: Props) {
  if (amount == null) return <span className="text-muted">—</span>;

  try {
    const formatted = new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency ?? 'EUR',
      maximumFractionDigits: 0,
    }).format(amount);
    return <span>{formatted}</span>;
  } catch {
    return <span>{amount} {currency}</span>;
  }
}
