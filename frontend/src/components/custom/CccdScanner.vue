<template>
  <n-modal :show="modelValue">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Quét QR CCCD</h2>
        <button class="close-btn" @click="closeModal">X</button>
      </div>

      <div class="video-container">
        <video ref="videoRef"></video>
      </div>

      <div v-if="decodedData" class="result-container">
        <h3>Kết quả CCCD:</h3>
        <table>
          <tbody>
            <tr>
              <td>Số CCCD</td>
              <td>{{ decodedData.soCCCD }}</td>
            </tr>
            <tr>
              <td>Họ tên</td>
              <td>{{ decodedData.hoTen }}</td>
            </tr>
            <tr>
              <td>Địa chỉ</td>
              <td>{{ decodedData.diaChi }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </n-modal>
</template>

<script setup>
import { ref, watch, nextTick, onBeforeUnmount } from "vue";
import { BrowserQRCodeReader } from "@zxing/library";
import { NModal } from "naive-ui";

const props = defineProps({
  modelValue: Boolean   // v-model từ cha
});

const emit = defineEmits(["update:modelValue", "scan-result"]);

const videoRef = ref(null);
const decodedData = ref(null);
const isScanning = ref(false);

let codeReader = null;

function parseCCCDQR(raw) {
  const parts = raw.split("|");
  console.log("Parsed [0]", parts[0]);
  console.log("Parsed [1]", parts[1]);
  console.log("Parsed [2]", parts[2]);
  console.log("Parsed [3]", parts[3]);
  console.log("Parsed [4]", parts[4]);
  console.log("Parsed [5]", parts[5]);
  return {
    soGiayTo: parts[0] || "",

    hoTen: parts[2] || "",
    ngaySinh: parts[3] || "",
    gioiTinh: parts[4] || "",
    diaChi: parts[5] || "",
  };
}

async function startScanner() {
  await nextTick(); // 💡 bảo đảm video mount

  if (!videoRef.value || isScanning.value) return;

  stopScanner(); // tránh bị chạy 2 lần

  isScanning.value = true;
  codeReader = new BrowserQRCodeReader();

  try {
    await codeReader.decodeFromVideoDevice(null, videoRef.value, (result) => {
      if (result) {
        const data = parseCCCDQR(result.getText());
        decodedData.value = data;

        emit("scan-result", data);   // gửi dữ liệu lên cha
        closeModal();                // tự tắt modal
      }
    });
  } catch (err) {
    console.error("Scanner Error:", err);
    isScanning.value = false;
  }
}

function stopScanner() {
  if (videoRef.value && videoRef.value.srcObject) {
    const stream = videoRef.value.srcObject;
    const tracks = stream.getTracks();
    tracks.forEach(track => track.stop());
    videoRef.value.srcObject = null;
  }
  codeReader?.reset?.();
  codeReader = null;
  decodedData.value = null;
  isScanning.value = false;
}

function closeModal() {
  stopScanner();
  emit("update:modelValue", false);  // tắt modal trong cha
}

/* mở cam khi modelValue = true */
watch(
  () => props.modelValue,
  (val) => {
    if (val) startScanner();
    else stopScanner();
  }
);

onBeforeUnmount(stopScanner);
</script>

<style scoped>
.modal-content {
  background: #fff;
  border-radius: 10px;
  max-width: 600px;
  width: 100%;
  padding: 15px;
}

.video-container {
  height: 500px;
  overflow: hidden;
  border-radius: 10px;
  border: 2px solid #3b82f6;
}

video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.modal-header {
  position: relative;
  /* 🔑 để nút close nằm trong header */
  display: flex;
  font-weight: 700;
  font-size: larger;
  padding: 13px 0;
}

.close-btn {
  position: absolute;
  /* góc trên phải của modal-header */
  top: 10px;
  right: 5px;

  width: 32px;
  height: 32px;
  border: none;
  background-color: red;
  border-radius: 5px;

  color: #ffffff;
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s ease, transform 0.2s ease;
}
</style>
