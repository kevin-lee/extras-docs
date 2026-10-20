import React, {useEffect, useMemo, useState} from 'react';
import Layout from '@theme/Layout';

import type {ReleaseNote, ReleaseNotesData} from './types';
import ReleaseItem from './ReleaseItem';
import styles from './styles.module.css';

type YearGroup = {
  year: string;
  releases: ReleaseNote[];
};

const filterWords = (filter: string): string[] => filter.trim().toLowerCase().split(/\s+/).filter(Boolean);

const matchesAll = (release: ReleaseNote, words: string[]): boolean =>
  words.every(word => release.searchText.includes(word));

const groupByYear = (releases: ReleaseNote[]): YearGroup[] =>
  releases.reduce<YearGroup[]>((groups, release) => {
    const year = release.date?.slice(0, 4) ?? 'Unknown';
    const last = groups[groups.length - 1];
    if (last && last.year === year) {
      last.releases.push(release);
      return groups;
    }
    return [...groups, {year, releases: [release]}];
  }, []);

const initialExpanded = (releases: ReleaseNote[]): Set<string> => {
  const first = releases.find(release => release.latest) ?? releases[0];
  return new Set(first ? [first.version] : []);
};

function NoRelease({repoUrl}: {repoUrl: string}): React.JSX.Element {
  return (
    <div className={`card ${styles.emptyState}`}>
      <div className="card__body">
        <h2>No release yet</h2>
        <p>There is no release of Extras yet. Release notes will show here after the first release.</p>
        <a href={repoUrl} target="_blank" rel="noopener noreferrer">
          Go to the GitHub repository
        </a>
      </div>
    </div>
  );
}

function ReleaseList({releaseNotes}: {releaseNotes: ReleaseNotesData}): React.JSX.Element {
  const {releases, repoUrl} = releaseNotes;

  const [expanded, setExpanded] = useState<Set<string>>(() => initialExpanded(releases));
  const [filter, setFilter] = useState('');
  const [savedExpanded, setSavedExpanded] = useState<Set<string> | null>(null);

  const words = filterWords(filter);
  const visibleReleases = useMemo(
    () => (words.length > 0 ? releases.filter(release => matchesAll(release, words)) : releases),
    // `words` is made from `filter`.
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [releases, filter],
  );
  const yearGroups = useMemo(() => groupByYear(visibleReleases), [visibleReleases]);

  const changeFilter = (nextFilter: string) => {
    const nextWords = filterWords(nextFilter);
    if (nextWords.length > 0) {
      if (words.length === 0) {
        setSavedExpanded(expanded);
      }
      setExpanded(
        new Set(releases.filter(release => matchesAll(release, nextWords)).map(release => release.version)),
      );
    } else if (words.length > 0) {
      setExpanded(savedExpanded ?? initialExpanded(releases));
      setSavedExpanded(null);
    }
    setFilter(nextFilter);
  };

  useEffect(() => {
    const versions = new Set(releases.map(release => release.version));
    const openFromHash = () => {
      const version = decodeURIComponent(window.location.hash.slice(1)).replace(/^v/, '');
      if (!versions.has(version)) {
        return;
      }
      setFilter('');
      setSavedExpanded(null);
      setExpanded(previous => new Set(previous).add(version));
      window.requestAnimationFrame(() => {
        document.getElementById(version)?.scrollIntoView({block: 'start'});
      });
    };
    openFromHash();
    window.addEventListener('hashchange', openFromHash);
    return () => window.removeEventListener('hashchange', openFromHash);
  }, [releases]);

  const toggle = (version: string) =>
    setExpanded(previous => {
      const next = new Set(previous);
      if (next.has(version)) {
        next.delete(version);
      } else {
        next.add(version);
      }
      return next;
    });

  const expandAll = () =>
    setExpanded(previous => new Set([...previous, ...visibleReleases.map(release => release.version)]));

  const collapseAll = () =>
    setExpanded(previous => {
      const next = new Set(previous);
      visibleReleases.forEach(release => next.delete(release.version));
      return next;
    });

  return (
    <>
      <div className={styles.toolbar}>
        <label htmlFor="release-notes-filter" className={styles.visuallyHidden}>
          Filter release notes
        </label>
        <input
          id="release-notes-filter"
          type="search"
          className={styles.filterInput}
          placeholder="Filter by text, e.g. hedgehog"
          value={filter}
          onChange={event => changeFilter(event.target.value)}
        />
        <button type="button" className="button button--secondary button--sm" onClick={expandAll}>
          Expand all
        </button>
        <button type="button" className="button button--secondary button--sm" onClick={collapseAll}>
          Collapse all
        </button>
        <span className={styles.status} aria-live="polite">
          Showing {visibleReleases.length} of {releases.length} releases
        </span>
      </div>

      {visibleReleases.length === 0 ? (
        <div className={styles.noMatch}>
          <p>No release matches “{filter.trim()}”.</p>
          <button type="button" className="button button--primary button--sm" onClick={() => changeFilter('')}>
            Clear filter
          </button>
        </div>
      ) : (
        yearGroups.map(group => (
          <section key={group.year} className={styles.yearGroup}>
            <h2 className={styles.yearHeading}>{group.year}</h2>
            {group.releases.map(release => (
              <ReleaseItem
                key={release.version}
                release={release}
                repoUrl={repoUrl}
                expanded={expanded.has(release.version)}
                onToggle={() => toggle(release.version)}
              />
            ))}
          </section>
        ))
      )}
    </>
  );
}

export default function ReleaseNotesPage({releaseNotes}: {releaseNotes: ReleaseNotesData}): React.JSX.Element {
  const {releases, repoUrl} = releaseNotes;
  return (
    <Layout title="Release Notes" description="Release notes of all Extras versions">
      <main className="container margin-vert--lg">
        <header className={styles.header}>
          <h1>Release Notes</h1>
          <p>All releases of Extras, newest first.</p>
          <p className={styles.headerLinks}>
            <a href={`${repoUrl}/releases`} target="_blank" rel="noopener noreferrer">
              GitHub Releases
            </a>
            <a href={`${repoUrl}/releases.atom`} target="_blank" rel="noopener noreferrer">
              Atom feed
            </a>
          </p>
        </header>
        {releases.length === 0 ? <NoRelease repoUrl={repoUrl} /> : <ReleaseList releaseNotes={releaseNotes} />}
      </main>
    </Layout>
  );
}
