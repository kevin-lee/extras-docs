import React, {useEffect, useState} from 'react';

const DayInMillis = 24 * 60 * 60 * 1000;

const toRelative = (date: string, now: Date): string => {
  const format = new Intl.RelativeTimeFormat('en', {numeric: 'auto'});
  const days = Math.floor((now.getTime() - new Date(`${date}T00:00:00Z`).getTime()) / DayInMillis);
  if (days < 30) {
    return format.format(-days, 'day');
  }
  const months = Math.floor(days / 30);
  if (months < 12) {
    return format.format(-months, 'month');
  }
  return format.format(-Math.floor(days / 365), 'year');
};

/**
 * Shows " · 5 days ago" after the page is mounted.
 * It shows nothing in the server render, so the hydration has no mismatch.
 */
export default function RelativeDate({date}: {date: string}): React.JSX.Element | null {
  const [relative, setRelative] = useState<string | null>(null);

  useEffect(() => {
    setRelative(toRelative(date, new Date()));
  }, [date]);

  return relative ? <span> · {relative}</span> : null;
}
