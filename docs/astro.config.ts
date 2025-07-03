// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import { plugin as constantsPlugin } from './src/util/remark/replace-constants'
import { LATEST_BESTIUM, LATEST_USERDEV } from './src/util/versions';

// https://astro.build/config
export default defineConfig({
    integrations: [
        starlight({
            title: 'Bestium Docs',
            social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/huzvanec/Bestium' }],
            favicon: '/favicon.ico',
            sidebar: [
                {
                    label: 'Start Here',
                    items: [
                        { label: 'About Bestium', slug: 'start/about-bestium' },
                    ]
                },
                {
                    label: 'Server Setup',
                    items: [
                        { label: 'Installation', slug: 'server/installation' },
                        { label: 'Usage', slug: 'server/usage' }
                    ]
                },
                {
                    label: 'Bestium API',
                    items: [
                        { label: 'Project setup', slug: 'code/project-setup' },
                        { label: 'Coding an entity', slug: 'code/coding-entity' },
                        { label: 'Injecting an entity', slug: 'code/injecting-entity' },
                    ]
                }
            ],
        }),
    ],
    markdown: {
        remarkPlugins: [
            [
                constantsPlugin,
                {
                    constants: {
                        LATEST_BESTIUM,
                        LATEST_USERDEV
                    }
                }
            ]
        ]
    }
});
