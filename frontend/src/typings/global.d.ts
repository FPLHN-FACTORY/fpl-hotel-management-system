import moment from 'moment'
import { $t } from '../utils/i18n'
/* Stores the database entity table type, the specific content is in ./entities */
declare namespace Entity {
}

/* The data type returned by various interfaces, the specific content is in ./api */
declare namespace Api {

}

declare global {
  interface Window {
    $loadingBar: import('naive-ui').LoadingBarApi
    $dialog: import('naive-ui').DialogApi
    $message: import('naive-ui').MessageApi
    $notification: import('naive-ui').NotificationApi
  }
}

declare const AMap: any
declare const BMap: any

declare module '*.vue' {
  import type { DefineComponent } from 'vue'

  const component: DefineComponent
  export default component
}

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $t: typeof $t
    $moment: typeof moment
    $router: Router
  }
}

declare namespace NaiveUI {
  type ThemeColor = 'default' | 'error' | 'primary' | 'info' | 'success' | 'warning'
}

// Fix the slots type of the TabPane component of naive-ui https://github.com/tusen-ai/naive-ui/issues/6779 , but this will cause more types of errors and you will not be able to view the source code of naive-ui
// declare module 'naive-ui' {
//   interface TabPaneSlots {
//     tab?: () => VNode[]
//   }
// }
declare namespace Storage {
  interface Session {
    dict: DictMap
  }

  interface Local {
    /* Store user information */
    userInfo: Api.Login.Info
    /* Store access token */
    accessToken: string
    /* Store refresh token */
    refreshToken: string
    /* Store login account */
    loginAccount: any
    /* Store current language */
    lang: App.lang
  }
}

declare namespace App {
  type lang = 'enUS' | 'viVN' | 'zhCN' | 'jaJP' | 'koKR'
}

interface DictMap {
  [key: string]: Entity.Dict[]
}
