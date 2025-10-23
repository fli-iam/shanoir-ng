import { defineConfig } from 'vite';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
    plugins: [angular()],
    server: {
        allowedHosts: ['shanoir-ng-nginx'],
        hmr: {
            host: 'shanoir-ng-nginx',
            protocol: 'wss',
            clientPort: 443
        }
    }
});
