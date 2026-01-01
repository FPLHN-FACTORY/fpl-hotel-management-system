<script setup lang="ts">
import { ref, onBeforeUnmount, watch } from 'vue'
import { NModal, NCard, NButton } from 'naive-ui'

const props = defineProps<{ show: boolean }>()
const emit = defineEmits(['update:show', 'success'])

const videoEl = ref<HTMLVideoElement | null>(null)
const scanCanvasEl = ref<HTMLCanvasElement | null>(null)

const error = ref('')
const debugInfo = ref('Đang tải công nghệ lõi...')
const debugDetails = ref('')
const zoomLevel = ref(2.0)
const isProcessing = ref(false)

let stream: MediaStream | null = null
let scanInterval: any = null
let detector: any = null
let hasJsQR = false

// 1. Inject jsQR as Universal Fallback
const script = document.createElement('script')
script.src = 'https://cdn.jsdelivr.net/npm/jsqr@1.4.0/dist/jsQR.min.js'
script.async = true
script.onload = () => {
    hasJsQR = true
    if (!detector) debugInfo.value = "Đã tải bộ giải mã (JS). Sẵn sàng."
}
document.head.appendChild(script)

const hasNativeDetector = 'BarcodeDetector' in window

async function startCamera() {
    try {
        error.value = ''
        debugInfo.value = 'Đang bật Camera...'

        // Use 4K if possible for digital zoom quality
        const constraints = {
            audio: false,
            video: {
                facingMode: 'environment',
                width: { ideal: 3840 },
                height: { ideal: 2160 },
                focusMode: 'continuous'
            }
        }

        stream = await navigator.mediaDevices.getUserMedia(constraints)

        if (videoEl.value) {
            videoEl.value.srcObject = stream
            videoEl.value.onloadedmetadata = () => {
                videoEl.value?.play()
                initDetector()
                startScanning()
            }
        }
    } catch (err: any) {
        error.value = `Lỗi Camera: ${err.message}`
    }
}

function initDetector() {
    const track = stream?.getVideoTracks()[0]
    if (track) {
        try {
            const caps = track.getCapabilities() as any
            if (caps.focusMode && caps.focusMode.includes('continuous')) {
                track.applyConstraints({ advanced: [{ focusMode: 'continuous' }] } as any)
            }
            if ('zoom' in caps) {
                track.applyConstraints({ advanced: [{ zoom: 2.0 }] } as any).catch(() => { })
            }
        } catch (e) { }
    }

    if (hasNativeDetector) {
        // @ts-ignore
        detector = new window.BarcodeDetector({ formats: ['qr_code'] })
        debugInfo.value = "Sử dụng: Native Hardware Detector (Siêu nhanh)"
    } else {
        debugInfo.value = "Trình duyệt chưa hỗ trợ Native. Chuyển sang chế độ Polyfill (jsQR)."
    }
}

function startScanning() {
    if (scanInterval) clearInterval(scanInterval)

    // Create Context once
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d', { willReadFrequently: true })

    scanInterval = setInterval(async () => {
        if (!videoEl.value || !scanCanvasEl.value || isProcessing.value) return
        if (!detector && !hasJsQR) return // Wait for at least one engine

        isProcessing.value = true
        try {
            const video = videoEl.value

            // Logic: CROP + PADDING for Distant scanning
            const vW = video.videoWidth
            const vH = video.videoHeight
            const cropW = vW / zoomLevel.value
            const cropH = vH / zoomLevel.value
            const cropX = (vW - cropW) / 2
            const cropY = (vH - cropH) / 2
            const padding = 60

            // Update internal canvas size
            canvas.width = cropW + (padding * 2)
            canvas.height = cropH + (padding * 2)

            if (!ctx) return

            // White Background (Quiet Zone)
            ctx.fillStyle = '#FFFFFF'
            ctx.fillRect(0, 0, canvas.width, canvas.height)

            // Draw Center Crop
            ctx.drawImage(video, cropX, cropY, cropW, cropH, padding, padding, cropW, cropH)

            // HYBRID DETECTION
            if (detector) {
                // Method A: Native
                try {
                    const codes = await detector.detect(canvas)
                    if (codes.length > 0) processResult(codes[0].rawValue)
                } catch (e) { } // Native fail? ignore
            } else if (hasJsQR) {
                // Method B: jsQR Fallback
                try {
                    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
                    // @ts-ignore
                    const code = window.jsQR(imageData.data, imageData.width, imageData.height, {
                        inversionAttempts: "dontInvert",
                    })
                    if (code) processResult(code.data)
                } catch (e) { }
            }

        } catch (e) {
            console.error("Frame error", e)
        } finally {
            isProcessing.value = false
        }
    }, 80)
}

function processResult(text: string) {
    if (!text) return
    const parts = text.split('|')
    debugDetails.value = `Len: ${text.length} | Parts: ${parts.length}`
    if (parts.length >= 6) {
        debugInfo.value = "THÀNH CÔNG!"
        emit('success', {
            cccd: parts[0],
            cmnd: parts[1],
            name: parts[2],
            dob: parts[3],
            gender: parts[4],
            address: parts[5],
            dateIssue: parts[6]
        })
        emit('update:show', false)
    } else {
        debugDetails.value = "Đang đọc... Giữ yên!"
    }
}

function updateZoom(val: number) {
    zoomLevel.value = val
}

watch(() => props.show, (val) => {
    if (val) startCamera()
    else performStop()
})

onBeforeUnmount(() => {
    performStop()
    if (script.parentNode) script.parentNode.removeChild(script)
})

function performStop() {
    if (scanInterval) clearInterval(scanInterval)
    if (stream) stream.getTracks().forEach(t => t.stop())
    stream = null
}
</script>

<template>
    <NModal :show="show" @update:show="$emit('update:show', $event)">
        <NCard style="width: 700px; max-width: 95vw" title="Quét CCCD (Hybrid Engine)" :bordered="false" size="huge"
            role="dialog" aria-modal="true">
            <div
                class="relative w-full h-[500px] bg-black rounded-lg overflow-hidden flex flex-col items-center justify-center">
                <div class="w-full h-full overflow-hidden relative">
                    <video ref="videoEl" class="w-full h-full object-cover"
                        :style="{ transform: `scale(${zoomLevel})` }" style="transition: transform 0.1s ease-out;" muted
                        playsinline></video>
                </div>
                <!-- Overlays -->
                <div class="absolute inset-0 pointer-events-none flex items-center justify-center">
                    <div
                        class="w-[300px] h-[300px] border-2 border-green-400 rounded-lg flex items-center justify-center shadow-lg">
                        <div class="w-2 h-2 bg-red-500 rounded-full animate-pulse"></div>
                    </div>
                </div>
                <div class="absolute top-0 w-full bg-black/60 text-white p-2 text-center text-xs">
                    <div class="font-bold text-green-300">{{ debugInfo }}</div>
                    <div class="text-orange-300 font-mono">{{ debugDetails }}</div>
                    <div v-if="error" class="text-red-500">{{ error }}</div>
                </div>
            </div>
            <!-- Controls -->
            <div class="mt-4 space-y-3">
                <div class="flex items-center gap-4 px-4 bg-gray-100 p-2 rounded">
                    <span class="font-bold text-gray-600">ZOOM:</span>
                    <input type="range" class="flex-1 cursor-pointer" min="1" max="5" step="0.2" :value="zoomLevel"
                        @input="(e: any) => updateZoom(parseFloat(e.target.value))" />
                    <span class="font-bold w-12 text-right">{{ zoomLevel }}x</span>
                </div>
                <div class="bg-blue-50 p-2 rounded text-center text-sm text-gray-600">
                    Cách 40-50cm + Canh giữa hồng tâm + Chỉnh Zoom cho đến khi nét
                </div>
            </div>
            <template #footer>
                <div class="flex justify-end">
                    <NButton @click="$emit('update:show', false)">Đóng</NButton>
                </div>
            </template>
        </NCard>
    </NModal>
    <!-- Hidden Canvas -->
    <canvas ref="scanCanvasEl" class="hidden"></canvas>
</template>


<style scoped>
/* Scoped styles */
</style>
