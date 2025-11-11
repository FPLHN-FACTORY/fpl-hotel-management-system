import type { NDateLocale, NLocale } from 'naive-ui'
import { i18n } from '@/modules/i18n'
import { dateZhCN, zhCN, enUS, dateEnUS } from 'naive-ui'
import { App } from '@/typings/global'

export function setLocale(locale: App.lang) {
  i18n.global.locale.value = locale
}

export const $t = i18n.global.t

export const naiveI18nOptions: Record<App.lang, { locale: NLocale | null, dateLocale: NDateLocale | null }> = {
  zhCN: {
    locale: zhCN,
    dateLocale: dateZhCN,
  },
  enUS: {
    locale: enUS,
    dateLocale: dateEnUS,
  },
  viVN: {
    locale: enUS,
    dateLocale: dateEnUS,
  },
  jaJP: {
    locale: enUS,
    dateLocale: dateEnUS,
  },
  koKR: {
    locale: enUS,
    dateLocale: dateEnUS,
  },
}
