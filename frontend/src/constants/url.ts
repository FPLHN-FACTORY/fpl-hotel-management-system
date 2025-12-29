export const { VITE_BASE_URL_SERVER } = import.meta.env || {}
export const { VITE_BASE_URL_CLIENT } = import.meta.env || {}

export const API_URL = `${VITE_BASE_URL_SERVER}/api/v1` as string

export const API_LE_TAN_PHONG = `${API_URL}/leTan/phong` as string
export const API_LE_TAN_LOAI_PHONG = `${API_URL}/leTan/loai-phong` as string
export const API_LE_TAN_LICH_DAT_PHONG = `${API_URL}/leTan/lich-dat-phong` as string
export const API_LE_TAN_SO_DO_PHONG = `${API_URL}/leTan/so-do-phong` as string
export const API_LE_TAN_TAG = `${API_URL}/leTan/tag` as string
export const API_ADMIN_NHAN_VIEN = `${API_URL}/leTan/nhan-vien` as string
export const API_LE_TAN_DAT_PHONG = `${API_URL}/leTan/dat-phong` as string


