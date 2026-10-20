import React from 'react';
import {Fragment, jsx, jsxs} from 'react/jsx-runtime';
import {toJsxRuntime, type Components} from 'hast-util-to-jsx-runtime';
import clsx from 'clsx';
import CodeBlock from '@theme/CodeBlock';

import type {HastRoot} from './types';
import styles from './styles.module.css';

type ReleaseCodeBlockProps = {
  'data-language'?: string;
  'data-code'?: string;
};

/** Renders the `release-code-block` element made by the release-notes plugin with the site code block theme. */
function ReleaseCodeBlock(props: ReleaseCodeBlockProps): React.JSX.Element {
  return <CodeBlock language={props['data-language']}>{props['data-code'] ?? ''}</CodeBlock>;
}

const components = {'release-code-block': ReleaseCodeBlock} as unknown as Partial<Components>;

export default function ReleaseBody({tree}: {tree: HastRoot}): React.JSX.Element {
  return (
    <div className={clsx('markdown', styles.releaseBody)}>
      {toJsxRuntime(tree, {Fragment, jsx, jsxs, components})}
    </div>
  );
}
