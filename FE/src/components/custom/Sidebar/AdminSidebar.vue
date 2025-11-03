<script setup lang="ts">
import { useSidebarStore } from "@/stores/sidebar";
import { ref } from "vue";
import SidebarItem from "./SidebarItem.vue";
import { ROUTES_CONSTANTS } from "@/constants/path";

const sidebarStore = useSidebarStore();

const menuGroups = ref([
  {
    menuItems: [

      {
        label: "Quản lý nhân viên",
        icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>`,
        routeName: ROUTES_CONSTANTS.ADMIN.children.NHAN_VIEN.name,
      },
      {
        label: "Quản lý phong",
        icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>`,
        routeName: ROUTES_CONSTANTS.ADMIN.children.PHONG.name,
      },
    ],
  },
]);

// Toggle sidebar function
const toggleSidebar = () => {
  sidebarStore.toggleSidebar();
};

// Check if sidebar is collapsed (thu gọn)
const isCollapsed = ref(false);

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
};
</script>

<template>
  <aside class="sidebar" :class="{
    'translate-x-0': !sidebarStore.isSidebarOpen,
    '-translate-x-full': sidebarStore.isSidebarOpen,
    'collapsed': isCollapsed
  }">
    <!-- Header with Logo and Toggle Button -->
    <div class="header">
      <!-- Toggle Button ở trên -->
      <div class="toggle-container">
        <!-- <button @click="toggleCollapse" class="toggle-button" type="button"
          :title="isCollapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar'" aria-label="Toggle sidebar collapse">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"
            class="toggle-icon" :class="{ 'rotate-180': isCollapsed }">
            <path stroke-linecap="round" stroke-linejoin="round" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
          </svg>
        </button> -->
        <div class="logo-container">
          <router-link to="/" class="logo-link">
            <img src="/images/logo.jpg" alt=""
              class="logo" :class="{ 'collapsed': isCollapsed }" style="margin-bottom: 10px;" />
          </router-link>
        </div>
      </div>

      <!-- Logo Container -->

    </div>

    <!-- Navigation Menu -->
    <nav class="menu" aria-label="Menu điều hướng chính">
      <template v-for="(menuGroup, groupIndex) in menuGroups" :key="groupIndex">
        <div>
          <ul class="list-unstyled" role="menu">
            <SidebarItem v-for="(menuItem, index) in menuGroup.menuItems" :item="menuItem" :key="index" :index="index"
              :isCollapsed="isCollapsed" class="nav-link" role="menuitem" />
          </ul>
        </div>
      </template>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  background-color: #ffffff;
  /* width: 300px; */
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease-in-out, transform 0.3s ease-in-out;
  color: #374151;
  border-right: 1px solid #e5e7eb;
  user-select: none;
  font-family: Roboto, sans-serif;
  position: relative;
  z-index: 100;
}

/* Trạng thái thu gọn */
.sidebar.collapsed {
  width: 70px;
}

.header {
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem 0.5rem 0.5rem 0.5rem;
  border-bottom: 1px solid #e5e7eb;
  min-height: 80px;
  position: relative;
}

.toggle-container {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  padding-bottom: 0.5rem;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-bottom: 10px;
}

.logo-link {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  width: 160px;
  max-width: 80%;
  height: auto;
  object-fit: contain;
  transition: opacity 0.3s ease;
}

/* Toggle Button Styles */
.toggle-button {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  width: 28px;
  height: 28px;
}

.toggle-button:hover {
  background-color: #59bddb;
  color: white;
  border-color: #59bddb;
}

.toggle-icon {
  width: 16px;
  height: 16px;
  transition: transform 0.3s ease;
}

.toggle-icon.rotate-180 {
  transform: rotate(180deg);
}

.menu {
  flex-grow: 1;
  overflow-y: auto;
  padding: 0.5rem 0;
  scrollbar-width: thin;
  scrollbar-color: #d1d5db transparent;
}

.menu::-webkit-scrollbar {
  width: 4px;
}

.menu::-webkit-scrollbar-track {
  background: transparent;
}

.menu::-webkit-scrollbar-thumb {
  background-color: #d1d5db;
  border-radius: 2px;
}

.menu::-webkit-scrollbar-thumb:hover {
  background-color: #9ca3af;
}

.list-unstyled {
  margin: 0;
  padding: 0 0.1rem;
  list-style: none;
}

/* Footer Styles */
.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid #e5e7eb;
  background-color: #f8fafc;
}

.version-info {
  text-align: center;
}

.version-text {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

/* Sidebar States */
.translate-x-0 {
  transform: translateX(0);
}

.-translate-x-full {
  transform: translateX(-100%);
}

/* Overlay for mobile */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 50;
  transition: opacity 0.3s ease;
}

/* Responsive Design */
@media (max-width: 768px) {
  .sidebar {
    width: 260px;
  }

  .sidebar.collapsed {
    width: 60px;
  }

  .menu {
    padding: 0.25rem 0;
  }

  .list-unstyled {
    padding: 0 0.75rem;
  }

  .sidebar.collapsed .list-unstyled {
    padding: 0 0.25rem;
  }
}

/* Focus styles for accessibility */
.toggle-button:focus {
  outline: 2px solid #59bddb;
  outline-offset: 2px;
}

.logo-link:focus {
  outline: 2px solid #59bddb;
  outline-offset: 4px;
  border-radius: 4px;
}
</style>
