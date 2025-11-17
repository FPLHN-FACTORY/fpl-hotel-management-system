<template>
  <div class="full-container p-4">
    <!-- Nút chọn màn hình -->
    <div class="flex items-center space-x-4">
      <n-button-group>
        <n-button :type="currentView === 'timeline' ? 'primary' : 'default'" @click="currentView = 'timeline'">
          Timeline
        </n-button>

        <n-button :type="currentView === 'map' ? 'primary' : 'default'" @click="currentView = 'map'">
          Sơ đồ
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
import SoDo from './sodo/soDo.vue';
import Timeline from './timeline/timeline.vue';

export default {
  components: { Timeline, SoDo },
  data() {
    return {
      currentView: 'map', // mặc định hiển thị Sơ đồ
      searchQuery: ''
    }
  },
  computed: {
    currentComponent() {
      switch (this.currentView) {
        case 'timeline': return 'Timeline';
        case 'map': return 'SoDo';
        default: return 'SoDo'; // fallback mặc định
      }
    }
  },
  methods: {
    handleSearch() {
      // xử lý tìm kiếm
    }
  }
}
</script>
