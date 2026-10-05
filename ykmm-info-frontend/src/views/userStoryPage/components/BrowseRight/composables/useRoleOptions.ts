import { ref, type Ref, watch } from 'vue'
import { listGroupedRolesAPI } from '@/api/role'
import type { RoleGroupVO } from '@/types/role'

export function useRoleOptions(editingMode: Ref<boolean>) {
  const roleOptions = ref<RoleGroupVO[]>([])

  async function loadRoles() {
    try {
      const res = await listGroupedRolesAPI()
      roleOptions.value = res.data ?? []
    } catch {
      roleOptions.value = []
    }
  }

  watch(
    editingMode,
    (val) => {
      if (val && roleOptions.value.length === 0) {
        void loadRoles()
      }
    },
    { immediate: true },
  )

  return { roleOptions, loadRoles }
}
