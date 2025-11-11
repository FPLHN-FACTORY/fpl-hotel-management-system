import type { App } from 'vue'
import { local } from '@/utils'
import { createI18n } from 'vue-i18n'
import enUS from '../locales/en_US.json'
import viVN from '../locales/vi_VN.json'
import zhCN from '../locales/zh_CN.json'
import jaJP from '../locales/ja_JP.json'
import koKR from '../locales/ko_KR.json'


const { VITE_DEFAULT_LANG } = import.meta.env

export const i18n = createI18n({
  legacy: false,
  locale: local.get('lang') || VITE_DEFAULT_LANG, //Default display language
  fallbackLocale: VITE_DEFAULT_LANG,
  messages: {
    enUS,
    viVN,
    zhCN,
    jaJP,
    koKR
  },
  // Missing internationalization key warning
  // missingWarn: false,

  // Missing fallback content warning
  fallbackWarn: false,
})

export function install(app: App) {
  app.use(i18n)
}
