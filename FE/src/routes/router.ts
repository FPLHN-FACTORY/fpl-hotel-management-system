import { ROUTES_CONSTANTS } from "@/constants/path";
import { ROLES } from "@/constants/roles";
import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

export const routes: RouteRecordRaw[] = [
  {
    path: ROUTES_CONSTANTS.REDIRECT.path,
    name: ROUTES_CONSTANTS.REDIRECT.name,
    component: () => import('@/routes/guard/Redirect.vue')
  },
  // Not Found route
  {
    path: ROUTES_CONSTANTS.NOT_FOUND.path,
    name: ROUTES_CONSTANTS.NOT_FOUND.name,
    component: () => import("@/pages/404/NotFound.vue"),
  },
  // 403 route
  {
    path: ROUTES_CONSTANTS.FORBIDDEN.path,
    name: ROUTES_CONSTANTS.FORBIDDEN.name,
    component: () => import("@/pages/403/Forbidden.vue"),
  },
  // 401 route
  {
    path: ROUTES_CONSTANTS.UNAUTHORIZED.path,
    name: ROUTES_CONSTANTS.UNAUTHORIZED.name,
    component: () => import("@/pages/401/Unauthorized.vue"),
  },

  {
    path: ROUTES_CONSTANTS.LOGIN.path,
    name: ROUTES_CONSTANTS.LOGIN.name,
    component: () => import('@/pages/auth/LoginAdmin.vue'),

  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.NHAN_VIEN.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.NHAN_VIEN.path,
        name: ROUTES_CONSTANTS.ADMIN.children.NHAN_VIEN.name,
        component: () => import("@/pages/admin/nhanvien/NhanVien.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
      {
        path: ROUTES_CONSTANTS.ADMIN.children.PHONG.path,
        name: ROUTES_CONSTANTS.ADMIN.children.PHONG.name,
        component: () => import("@/pages/admin/phong/Phong.vue"),
      },
    ],
  },

];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});
