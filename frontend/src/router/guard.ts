import type { Router } from 'vue-router'
import { useAppStore, useRouteStore, useTabStore } from '@/store'
import { local } from '@/utils'

const title = import.meta.env.VITE_APP_NAME

export function setupRouterGuard(router: Router) {
  const appStore = useAppStore()
  const routeStore = useRouteStore()
  const tabStore = useTabStore()

  router.beforeEach(async (to, from, next) => {
    // Determine whether it is an external link. If it is, open the web page directly and intercept the jump
    if (to.meta.href) {
      window.open(to.meta.href)
      return false
    }
    // Start loadingBar
    appStore.showProgress && window.$loadingBar?.start()

    // Determine whether there is a TOKEN and log in for authentication
    const isLogin = Boolean(local.get('accessToken'))
    if (!isLogin) {
      if (to.name === 'login')
        next()

      if (to.name !== 'login') {
        const redirect = to.name === '404' ? undefined : to.fullPath
        next({ path: '/login', query: { redirect } })
      }
      return false
    }

    // Determine whether the route is initialized
    if (!routeStore.isInitAuthRoute) {
      await routeStore.initAuthRoute()
      // After dynamic routing is loaded, return to the root routing
      if (to.name === '404') {
        // Wait for the permission routing to be loaded, return to the previous routing, otherwise 404
        next({
          path: to.fullPath,
          replace: true,
          query: to.query,
          hash: to.hash,
        })
        return false
      }
    }

    // Determine whether the current page is logged in, then locate the home page
    if (to.name === 'login') {
      next({ path: '/' })
      return false
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
