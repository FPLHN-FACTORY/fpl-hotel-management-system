<template>
  <div class="container p-4">
    <!-- Nút chọn màn hình -->
<div class="flex items-center space-x-4">
    <n-button-group>
      <n-button :type="currentView === 'list' ? 'primary' : 'default'" @click="currentView = 'list'">
        Sơ đồ
      </n-button>

      <n-button :type="currentView === 'timeline' ? 'primary' : 'default'" @click="currentView = 'timeline'">
        Timeline
      </n-button>

      <n-button :type="currentView === 'map' ? 'primary' : 'default'" @click="currentView = 'map'">
        Danh sách
      </n-button>
    </n-button-group>

    <!-- Input tìm kiếm -->
    <n-input 
      v-model="searchQuery" 
      placeholder="Tìm kiếm khách hàng, mã đặt phòng..." 
      clearable
      @input="handleSearch"
      width="250px"
    >
      <template #prefix>
    <n-icon-wrapper :size="26" color="var(--success-color)" :border-radius="999">
            <nova-icon :size="18" icon="carbon:search" color="black" />
          </n-icon-wrapper>
      </template>
      </n-input>
  </div>


    <!-- Màn hình hiển thị -->
    <div class="mt-4">
      <component :is="currentComponent" />
    </div>
  </div>
</template>

<script>
import DanhSach from './danhSach/danhSach.vue';
import SoDo from './sodo/soDo.vue';
import Timeline from './timeline/timeline.vue';



export default {
  components: { DanhSach, Timeline, SoDo },
  data() {
    return {
      currentView: 'list' // mặc định hiển thị danh sách
    }
  },
  computed: {
    currentComponent() {
      switch (this.currentView) {
        case 'list': return 'DanhSach'
        case 'timeline': return 'Timeline'
        case 'map': return 'SoDo'
      }
    }
  }
}
</script>
