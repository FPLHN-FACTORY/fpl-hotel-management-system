<script setup lang="ts">
import { naiveI18nOptions } from '@/utils'
import { darkTheme } from 'naive-ui'
import { useAppStore } from './store'

const appStore = useAppStore()

const naiveLocale = computed(() => {
  return naiveI18nOptions[appStore.lang] ? naiveI18nOptions[appStore.lang] : naiveI18nOptions.enUS
})

// Auto-zoom for responsive consistency
// Target resolution width: 1600px (Balanced Desktop)
function adjustZoom() {
  const targetWidth = 1600
  const width = window.innerWidth
  const fixedZoom = 0.75
  if (width < targetWidth) {
    const zoomC = width / targetWidth
    // Limit min zoom to avoid text becoming unreadable
    const finalZoom = Math.max(zoomC, fixedZoom)
    // Use transform instead of zoom to avoid tooltip positioning issues
    document.body.style.transform = `scale(${finalZoom})`
    document.body.style.transformOrigin = 'top left'
    document.body.style.width = `${100 / finalZoom}%`
    document.body.style.height = `${100 / finalZoom}%`
    document.body.style.overflowX = 'hidden' // Prevent horizontal scrollbar
  } else {
    document.body.style.transform = 'none'
    document.body.style.width = '100%'
    document.body.style.height = '100%'
    document.body.style.overflowX = 'auto'
  }
}

onMounted(() => {
  adjustZoom()
  window.addEventListener('resize', adjustZoom)
})

onUnmounted(() => {
  window.removeEventListener('resize', adjustZoom)
  document.body.style.transform = 'none'
  document.body.style.width = '100%'
  document.body.style.height = '100%'
})
</script>

<template>
  <n-config-provider class="wh-full" inline-theme-disabled :theme="appStore.colorMode === 'dark' ? darkTheme : null"
    :locale="naiveLocale.locale" :date-locale="naiveLocale.dateLocale" :theme-overrides="appStore.theme">
    <naive-provider>
      <router-view />
      <Watermark :show-watermark="appStore.showWatermark" />
    </naive-provider>
  </n-config-provider>
</template>
