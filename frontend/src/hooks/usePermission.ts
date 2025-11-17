import { useAuthStore } from '@/store'
import { isArray, isString } from 'radash'

/** Permission judgment */
export function usePermission() {
  const authStore = useAuthStore()

  function hasPermission(
    permission?: Entity.RoleType | Entity.RoleType[],
  ) {
    if (!permission)
      return true

    if (!authStore.userInfo)
      return false
    const { role } = authStore.userInfo

    // If the role is super, it can be passed directly
    let has = role.includes('super')
    if (!has) {
      if (isArray(permission))
        // Role is an array, determine whether there is an intersection
        has = permission.some(i => role.includes(i))

      if (isString(permission))
        // Role is a string, check whether it contains
        has = role.includes(permission)
    }
    return has
  }

  return {
    hasPermission,
  }
}
