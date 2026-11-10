export function formatReturnTime(value: string): string {
  const match = /^(?:([01]\d|2[0-3])):([0-5]\d)$/.exec(value);
  if (!match) return value;

  const hour = Number(match[1]);
  const displayHour = hour % 12 || 12;
  const period = hour < 12 ? "a. m." : "p. m.";
  return `${String(displayHour).padStart(2, "0")}:${match[2]} ${period}`;
}
