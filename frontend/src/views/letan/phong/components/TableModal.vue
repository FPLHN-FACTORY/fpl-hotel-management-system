<template>
  <n-modal
    v-model:show="modalVisible"
    :mask-closable="false"
    preset="card"
    :title="title"
    class="w-700px modal-custom-font"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <n-form label-placement="left" :model="formModel" label-align="left" :label-width="150">
        <n-grid :cols="24" :x-gap="18">
          <n-form-item-grid-item :span="24" label="Tên phòng" path="tenPhong">
            <n-input
              v-model:value="formModel.tenPhong"
              placeholder="Nhập tên phòng"
              :disabled="isTenPhongDisabled"
            />
          </n-form-item-grid-item>

          <!-- Giữ nguyên các form item khác -->
          <!-- ... -->

        </n-grid>
      </n-form>
    </n-spin>

    <template #action>
      <n-space justify="center">
        <n-button @click="closeModal">
          Hủy
        </n-button>
        <n-button type="primary" @click="handleSubmit">
          Lưu
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-700px {
  width: 700px;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 18px;
  font-weight: 600;
}

.modal-custom-font :deep(.n-form-item-label) {
  font-size: 17px;
}

.modal-custom-font :deep(.n-input__input-el),
.modal-custom-font :deep(.n-input__textarea-el),
.modal-custom-font :deep(.n-base-selection-label),
.modal-custom-font :deep(.n-base-selection-input),
.modal-custom-font :deep(.n-input-number-input),
.modal-custom-font :deep(.n-button__content) {
  font-size: 17px;
}

.modal-custom-font :deep(.n-base-select-option__content) {
  font-size: 17px;
}
</style>
