import type { App } from 'vue'
import { installRouter } from '@/router'
import { installPinia } from '@/store'
import AppVue from './App.vue'
import AppLoading from './components/common/AppLoading.vue'

async function setupApp() {
  // Load global loading status
  const appLoading = createApp(AppLoading)
  appLoading.mount('#appLoading')

  // Create a vue instance
  const app = createApp(AppVue)

  // Register module Pinia
  await installPinia(app)

  // Register module Vue-router
  await installRouter(app)

  /* Register module directive/static resource */
  Object.values(
    import.meta.glob<{ install: (app: App) => void }>('./modules/*.ts', {
      eager: true,
    }),
  ).map(i => app.use(i))

  // Unmount loading animation
  appLoading.unmount()

  // Mount
  app.mount('#app')
}

setupApp()
