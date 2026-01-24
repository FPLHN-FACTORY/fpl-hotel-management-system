<script setup lang="ts">
import { naiveI18nOptions } from '@/utils'
import { darkTheme } from 'naive-ui'
import { useAppStore } from './store'

const appStore = useAppStore()

const windowWidth = ref(window.innerWidth)

function handleResize() {
  windowWidth.value = window.innerWidth
}

const isHeaderHidden = ref(false)
let lastScrollTop = 0

function handleScroll(e: Event) {
  const target = e.target as HTMLElement
  if (!target || typeof target.scrollTop === 'undefined') return
  
  // Only apply to main content scroll (containers that are likely the layout scroll)
  if (!target.classList.contains('n-layout-scroll-container') && target.tagName !== 'DIV') return

  const scrollTop = target.scrollTop
  
  // Throttle/Debounce check to avoid jitter
  if (Math.abs(scrollTop - lastScrollTop) < 10) return

  if (scrollTop > lastScrollTop && scrollTop > 150) {
    isHeaderHidden.value = true
  } else if (scrollTop < lastScrollTop) {
    isHeaderHidden.value = false
  }
  
  lastScrollTop = scrollTop
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  window.addEventListener('scroll', handleScroll, true) // Catch internal scrolls
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('scroll', handleScroll, true)
})

const naiveLocale = computed(() => {
  return naiveI18nOptions[appStore.lang] ? naiveI18nOptions[appStore.lang] : naiveI18nOptions.enUS
})

const dynamicThemeOverrides = computed(() => {
  const width = windowWidth.value
  const isMobile = width < 768
  // Logical width for laptops < 15" with scaling is usually between 1024 and 1550
  const isSmallLaptop = width >= 768 && width <= 1550 

  // Simulation of "80% look": Base font size reduced
  const baseSize = isMobile ? 13 : isSmallLaptop ? 13.5 : 15.5
  
  // Dynamic spacing
  const cardPadding = isMobile ? '10px' : isSmallLaptop ? '11px' : '20px'
  const gridGap = isMobile ? 10 : isSmallLaptop ? 12 : 24
  const cardBorderRadius = isSmallLaptop ? '10px' : '16px'

  // Base overrides from store
  const baseOverrides = JSON.parse(JSON.stringify(appStore.theme || {}))
  const common = baseOverrides.common || {}

  common.fontSize = `${baseSize}px`
  common.fontSizeMedium = `${baseSize}px`
  common.fontSizeSmall = `${baseSize - 2}px`
  common.fontSizeLarge = `${baseSize + 2}px`
  common.borderRadius = cardBorderRadius
  baseOverrides.common = common

  // Component refinements
  baseOverrides.Card = {
    ...baseOverrides.Card,
    paddingMedium: cardPadding,
    borderRadius: cardBorderRadius,
    fontSizeMedium: `${baseSize}px`,
    titleFontSizeMedium: `${baseSize + 1}px`,
  }

  baseOverrides.Grid = {
    ...baseOverrides.Grid,
    gap: `${gridGap}px`,
  }

  baseOverrides.Button = {
    ...baseOverrides.Button,
    heightMedium: isMobile ? '36px' : isSmallLaptop ? '32px' : '38px',
    fontSizeMedium: `${baseSize}px`,
    paddingMedium: '0 12px',
    borderRadius: '6px',
  }

  baseOverrides.Input = {
    ...baseOverrides.Input,
    heightMedium: isMobile ? '36px' : isSmallLaptop ? '32px' : '38px',
    fontSizeMedium: `${baseSize}px`,
  }

  baseOverrides.DataTable = {
    ...baseOverrides.DataTable,
    fontSizeMedium: `${baseSize - 1}px`,
    thPaddingMedium: isSmallLaptop ? '8px 4px' : '12px 8px',
    tdPaddingMedium: isSmallLaptop ? '8px 4px' : '12px 8px',
  }

  baseOverrides.Layout = {
    ...baseOverrides.Layout,
    siderColor: '#ffffff',
    headerColor: '#ffffff',
    color: '#ffffff',
  }

  return baseOverrides
})
</script>

<template>
  <n-config-provider 
    class="wh-full" 
    :class="{ 'header-hidden': isHeaderHidden }"
    inline-theme-disabled 
    :theme="appStore.colorMode === 'dark' ? darkTheme : null"
    :locale="naiveLocale.locale" 
    :date-locale="naiveLocale.dateLocale" 
    :theme-overrides="dynamicThemeOverrides"
  >
    <naive-provider>
      <router-view />
      <Watermark :show-watermark="appStore.showWatermark" />
    </naive-provider>
  </n-config-provider>
</template>
