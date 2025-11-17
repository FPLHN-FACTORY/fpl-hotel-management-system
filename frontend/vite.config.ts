import { resolve } from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import { createVitePlugins } from './src/configs/plugins'
import { createViteProxy } from './src/configs/proxy'
import { serviceConfig } from './src/configs/service.config'
// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // Load .env file based on `mode` in current working directory
  const env = loadEnv(mode, __dirname, '') as ImportMetaEnv
  const envConfig = serviceConfig[mode as ServiceEnvType]
  return {
    base: env.VITE_BASE_URL,
    plugins: createVitePlugins(env),
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    server: {
      host: '0.0.0.0',
      proxy:
        env.VITE_HTTP_PROXY === 'Y' ? createViteProxy(envConfig) : undefined,
    },
    build: {
      target: 'esnext',
      reportCompressedSize: false, // Enable/disable gzip compressed size reporting
    },
    optimizeDeps: {
      include: ['echarts', 'md-editor-v3', 'quill'],
    },
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern',
        },
      },
    },
  }
})
