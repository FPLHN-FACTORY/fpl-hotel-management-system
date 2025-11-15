export const { VITE_BASE_URL_SERVER } = import.meta.env || {}
export const { VITE_BASE_URL_CLIENT } = import.meta.env || {}

export const API_URL = `${ VITE_BASE_URL_SERVER }/api/v1` as string

export const API_LE_TAN_PHONG = `${API_URL}/leTan/phong` as string
export const API_LE_TAN_LOAI_PHONG = `${API_URL}/leTan/loai-phong` as string
