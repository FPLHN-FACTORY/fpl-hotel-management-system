import type { Router } from 'vue-router'
import { useAppStore, useRouteStore, useTabStore } from '@/store'
import { local } from '@/utils'

const title = import.meta.env.VITE_APP_NAME

export function setupRouterGuard(router: Router) {
  const appStore = useAppStore()
  const routeStore = useRouteStore()
  const tabStore = useTabStore()

  router.beforeEach(async (to, from, next) => {
  if (to.meta.href) {
    window.open(to.meta.href)
    return false
  }

  appStore.showProgress && window.$loadingBar?.start()

  const isLogin = Boolean(local.get('accessToken'))

  if (!isLogin) {
    if (to.name === 'login') {
      next()
      return
    }
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (!routeStore.isInitAuthRoute) {
    await routeStore.initAuthRoute()
    if (to.name === '404') {
      next({ path: to.fullPath, replace: true })
      return
    }
  }

  if (to.name === 'login') {
    next({ path: '/' })
    return
  }

  next()
})

  router.beforeResolve((to) => {
    // Set menu highlight
    routeStore.setActiveMenu(to.meta.activeMenu ?? to.fullPath)
    // Add tabs
    tabStore.addTab(to)
    // Set highlight tag;
    tabStore.setCurrentTab(to.fullPath as string)
  })

  router.afterEach((to) => {
    // Modify the page title
    document.title = `${to.meta.title} - ${title}`
    // End loadingBar
    appStore.showProgress && window.$loadingBar?.finish()
  })
}
