import fs from 'fs';
import path from 'path';

import type {LoadContext, Plugin} from '@docusaurus/types';
import {normalizeUrl} from '@docusaurus/utils';

import type {RawRelease, ReleaseNotesData} from '../../src/components/ReleaseNotes/types';

import {processRelease} from './markdown';

export type ReleaseNotesPluginOptions = {
  repoUrl: string;
  /** Relative to the site folder. */
  dataFile?: string;
  routeBasePath?: string;
};

/**
 * Makes the `/release-notes` page from `releases.json`.
 * A missing `releases.json` means that there is no release yet.
 */
export default function releaseNotesPlugin(
  context: LoadContext,
  options: ReleaseNotesPluginOptions,
): Plugin<ReleaseNotesData> {
  const dataFile = path.resolve(context.siteDir, options.dataFile ?? 'releases.json');
  const routeBasePath = options.routeBasePath ?? 'release-notes';

  return {
    name: 'docusaurus-plugin-release-notes',

    getPathsToWatch() {
      return [dataFile];
    },

    async loadContent() {
      const rawReleases: unknown = fs.existsSync(dataFile)
        ? JSON.parse(await fs.promises.readFile(dataFile, 'utf8'))
        : [];
      if (!Array.isArray(rawReleases)) {
        throw new Error(`[release-notes] ${dataFile} must have a JSON array of releases.`);
      }
      const releases = rawReleases as RawRelease[];
      const latestTagName = releases.find(release => !release.prerelease)?.tagName ?? null;

      return {
        repoUrl: options.repoUrl,
        releases: releases.map(release => processRelease(release, options.repoUrl, latestTagName)),
      };
    },

    async contentLoaded({content, actions}) {
      const releaseNotesDataPath = await actions.createData('release-notes.json', content);
      actions.addRoute({
        path: normalizeUrl([context.baseUrl, routeBasePath]),
        component: '@site/src/components/ReleaseNotes/index.tsx',
        modules: {releaseNotes: releaseNotesDataPath},
        exact: true,
      });
    },
  };
}
