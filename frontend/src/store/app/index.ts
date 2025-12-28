import type { GlobalThemeOverrides } from 'naive-ui'
import { local, setLocale } from '@/utils'
import { colord } from 'colord'
import { set } from 'radash'
import themeConfig from './theme.json'
import { App } from '@/typings/global'

export type TransitionAnimation = '' | 'fade-slide' | 'fade-bottom' | 'fade-scale' | 'zoom-fade' | 'zoom-out'
export type LayoutMode = 'leftMenu' | 'topMenu' | 'mixMenu'

const { VITE_COPYRIGHT_INFO } = import.meta.env

const docEle = ref(document.documentElement)

const { isFullscreen, toggle } = useFullscreen(docEle)

// Bỏ useColorMode, sử dụng giá trị cố định
const colorMode = ref('light')

export const useAppStore = defineStore('app-store', {
  state: () => {
    return {
      footerText: VITE_COPYRIGHT_INFO,
      lang: 'viVN' as App.lang, // Luôn là tiếng Việt
      theme: themeConfig as GlobalThemeOverrides,
      primaryColor: themeConfig.common.primaryColor,
      collapsed: false,
      grayMode: false,
      colorWeak: false,
      loadFlag: true,
      showLogo: true,
      showTabs: true,
      showFooter: true,
      showProgress: true,
      showBreadcrumb: true,
      showBreadcrumbIcon: true,
      showWatermark: false,
      showSetting: false,
      transitionAnimation: 'fade-slide' as TransitionAnimation,
      layoutMode: 'leftMenu' as LayoutMode,
      contentFullScreen: false,
    }
  },
  getters: {
    storeColorMode() {
      return 'light' // Luôn trả về light
    },
    colorMode() {
      return 'light' // Luôn trả về light
    },
    fullScreen() {
      return isFullscreen.value
    },
  },
  actions: {
    // Reset All Settings
    resetAlltheme() {
      this.theme = themeConfig
      this.primaryColor = '#18a058'
      this.collapsed = false
      this.grayMode = false
      this.colorWeak = false
      this.loadFlag = true
      this.showLogo = true
      this.showTabs = true
      this.showFooter = true
      this.showBreadcrumb = true
      this.showBreadcrumbIcon = true
      this.showWatermark = false
      this.transitionAnimation = 'fade-slide'
      this.layoutMode = 'leftMenu'
      this.contentFullScreen = false

      // Reset all colors
      this.setPrimaryColor(this.primaryColor)
    },
    setAppLang(lang: App.lang) {
      // Luôn giữ tiếng Việt, không cho thay đổi
      this.lang = 'viVN'
      setLocale('viVN')
      local.set('lang', 'viVN')
    },
    /* Set theme color */
    setPrimaryColor(color: string) {
      const brightenColor = colord(color).lighten(0.05).toHex()
      const darkenColor = colord(color).darken(0.05).toHex()
      set(this.theme, 'common.primaryColor', color)
      set(this.theme, 'common.primaryColorHover', brightenColor)
      set(this.theme, 'common.primaryColorPressed', darkenColor)
      set(this.theme, 'common.primaryColorSuppl', brightenColor)
    },
    setColorMode(mode: 'light' | 'dark' | 'auto') {
      // Không làm gì cả, luôn giữ light mode
      colorMode.value = 'light'
    },
    /* Toggle sidebar collapse */
    toggleCollapse() {
      this.collapsed = !this.collapsed
    },
    /* Toggle full screen */
    toggleFullScreen() {
      toggle()
    },
    /**
  * @description: Page content reload
  * @param {number} delay - delay in milliseconds
  * @return {*}
  */
    async reloadPage(delay = 600) {
      this.loadFlag = false
      await nextTick()
      if (delay) {
        setTimeout(() => {
          this.loadFlag = true
        }, delay)
      }
      else {
        this.loadFlag = true
      }
    },
    /* Switch to color blindness mode */
    toggleColorWeak() {
      docEle.value.classList.toggle('color-weak')
      this.colorWeak = docEle.value.classList.contains('color-weak')
    },
    /* Toggle Gray Mode */
    toggleGrayMode() {
      docEle.value.classList.toggle('gray-mode')
      this.grayMode = docEle.value.classList.contains('gray-mode')
    },
  },
  persist: {
    storage: localStorage,
  },
})
