<template>
  <n-modal :show="modelValue">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Quét OCR</h2>
        <button class="close-btn" @click="closeModal">X</button>
      </div>
      <div class="video-container">
        <video ref="videoRef"></video>
        <canvas ref="canvasRef" style="display:none;"></canvas>
      </div>
 
      <div class="container-btn">
        <!-- Nút chụp từ camera -->
        <button class="class-btn btn-camera" @click="captureAndOCR">
          <CameraOutline class="icon" style="width:24px; height:24px;" />
          Chụp
        </button>

        <!-- Nút upload ảnh -->
        <label class="class-btn btn-upload">
          <CloudUploadOutline style="width:24px; height:24px;" />

          Upload Ảnh
          <input type="file" accept="image/*" @change="uploadImage" />
        </label>
      </div>

      <!-- <div v-if="ocrResult">
        <h3>Kết quả OCR:</h3>
        <p>Loại giấy tờ: {{ ocrResult.loaiGiayTo }}</p>
        <p>Họ tên: {{ ocrResult.hoTen }}</p>
        <p>Số giấy tờ: {{ ocrResult.soGiayTo }}</p>
        <p>Ngày sinh: {{ ocrResult.ngaySinh }}</p>
        <p>Giới tính: {{ ocrResult.gioiTinh }}</p>
        <p>Địa chỉ: {{ ocrResult.diaChi }}</p>
                <p>Quốc tịch: {{ ocrResult.quocTich }}</p>
                        <p>Có giá trị đến: {{ ocrResult.coGiaTriDen }}</p>
                          
      </div> -->
    </div>
  </n-modal>
</template>


<script setup>
import { ref, nextTick, watch } from 'vue'
import Tesseract from 'tesseract.js'
import { CameraOutline, CloudUploadOutline} from '@vicons/ionicons5'
import { Camera } from '@vicons/carbon'
import { NModal } from 'naive-ui'

const videoRef = ref(null)
const canvasRef = ref(null)
const ocrRawText = ref('')
const ocrResult = ref({})

const props = defineProps({
  modelValue: Boolean // v-model từ cha
})

const emits = defineEmits(['update:modelValue', 'result']) // giống CccdScanner

let stream = null

const stopCamera = () => {
  if (stream) {
    stream.getTracks().forEach(t => t.stop())
    stream = null
  }
  if (videoRef.value) {
    if (videoRef.value.srcObject) {
      const tracks = videoRef.value.srcObject.getTracks();
      tracks.forEach(t => t.stop());
    }
    videoRef.value.srcObject = null
  }
}
const closeModal = () => {
  stopCamera()
  emits('update:modelValue', false)
}

const startCamera = async () => {
  if (!videoRef.value) return
  stopCamera()
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment' }
    })
    videoRef.value.srcObject = stream
  } catch (err) {
    console.error('Không thể mở camera:', err)
  }
}


watch(() => props.modelValue, async (val) => {
  if (val) {
    await nextTick() // đợi video mount
    startCamera()
  } else {
    stopCamera()
  }
})


const captureAndOCR = async () => {
  const video = videoRef.value
  const canvas = canvasRef.value

  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  const ctx = canvas.getContext('2d')
  ctx.drawImage(video, 0, 0)
  ctx.filter = 'contrast(1.3) brightness(1.1)'
  ctx.drawImage(canvas, 0, 0)

  await doOCR(canvas)
}

const uploadImage = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  const img = new Image()
  img.src = URL.createObjectURL(file)
  img.onload = async () => {
    const canvas = canvasRef.value
    canvas.width = img.width
    canvas.height = img.height
    const ctx = canvas.getContext('2d')
    ctx.drawImage(img, 0, 0)

    ctx.filter = 'contrast(1.3) brightness(1.1)'
    ctx.drawImage(canvas, 0, 0)

    await doOCR(canvas)
  }
}
const  determineDocumentType=(text) =>{
  const norm = text.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '')
    if (/P<|B\d{7,8}/.test(text)) {
    console.log("1 ok ")
    return 'Passport'
  }

  // Nếu có từ CCCD/CMND/CCCD/ID
  if (  /(can\s*cuoc\s*cong\s*dan|cccd|cmnd|cmt|citizen\s*(identity|ldentty)\s*card)/i.test(norm)) {
    console.log("0 ok ")
    return 'CCCD'
  }
  
  // Nếu có MRZ hoặc chữ "P<VNM"

  // Nếu không xác định được
  return 'Unknown'
}
const doOCR = async (canvas) => {
  try {
    const { data: { text } } = await Tesseract.recognize(
      canvas,
      'vie+eng',
      { tessedit_char_whitelist: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789/ ,-' }
    )

    ocrRawText.value = text
console.log("text",text)
    const docType = determineDocumentType(text)

    if (docType === 'Passport') {
      ocrResult.value = parsePassport(text)
      console.log("ocrResult",ocrResult.value)
    } else if (docType === 'CCCD') {
       console.log("ocrResult",ocrResult.value)
      ocrResult.value = parseCCCD(text)
    } else {
      ocrResult.value = { loaiGiayTo: 'Unknown' }
    }

    emits('result', ocrResult.value)
  } catch (err) {
    console.error('OCR Error:', err)
    alert('Lỗi khi đọc ảnh. Vui lòng thử lại!')
  }
}


const parseCCCD = (text) => {
  const result = {}
  const lines = text.split('\n').map(l => l.trim()).filter(l => l.length > 2)
const norm = normalizeOCRText(text);

const CCCD_REGEX =
  /(can\s*cuoc\s*cong\s*dan|cccd|cmnd|cmt|citizen\s*(identity|ldentty)\s*card)/i;

if (CCCD_REGEX.test(norm) || /\b\d{12}\b/.test(norm)) {
  result.loaiGiayTo = 0;
}



  // 1. Số CCCD (12 số)
  const cccdMatch = text.match(/\d{12}/)
  if (cccdMatch) result.soGiayTo = cccdMatch[0]

  // 2. Ngày sinh
  const dobMatch = text.match(/(\d{2}[\/\-]\d{2}[\/\-]\d{4})/)
  if (dobMatch) result.ngaySinh = dobMatch[0]

  // 3. Giới tính
  if (/NAM|MALE/i.test(text)) result.gioiTinh = 'Nam'
  else if (/NỮ|NU|FEMALE/i.test(text)) result.gioiTinh = 'Nữ'

  // 4. Họ tên
  const blacklistWords = ['CAN', 'CUOC', 'CONG', 'DAN', 'NGAY', 'SINH', 'DOB', 'GIOI', 'TINH', 'SEX', 'QUOC', 'TICH', 'QUE', 'QUAN', 'NOI', 'THUONG', 'TRU', 'PLACE', 'ORIGIN', 'RESIDENCE', 'DATE', 'BIRTH']

  let cccdLineIndex = -1
  for (let i = 0; i < lines.length; i++) {
    if (/\d{12}/.test(lines[i])) {
      cccdLineIndex = i
      break
    }
  }

  if (cccdLineIndex >= 0) {
    for (let i = cccdLineIndex + 1; i < Math.min(cccdLineIndex + 4, lines.length); i++) {
      let line = lines[i].trim()
      console.log(lines)

      const upperLine = line.toUpperCase()

      if (blacklistWords.some(w => upperLine.includes(w))) continue

      const words = line.split(/\s+/).filter(w => w.length >= 2)
      const nameWords = words.filter(w => /^[A-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠƯẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼẾỀỂỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪỬỮỰỲỴỶỸ]+$/.test(w))

      if (nameWords.length >= 2 && nameWords.length <= 4) {
        const cleanName = nameWords.join(' ')
        if (!/\d|[%$#@*]/.test(cleanName)) {
          result.hoTen = cleanName
          break
        }
      }
    }
  }

  // 5. NƠI THƯỜNG TRÚ
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    const u = line.toUpperCase()

    if (
      u.includes('NOI') ||
      u.includes('THUONG') ||
      u.includes('TDP') ||
      u.includes('RESID') ||
      u.includes('MSADENCE') ||
      u.includes('PXOC')
    ) {
      let addressParts = []

      let sameLine = line
        .replace(/.*?(NOI|NƠI).{0,30}/i, '')
        .replace(/PXOC.{0,20}|PLACE.{0,20}/i, '')
        .replace(/MSADENCE|RESIDENCE|RESIDINCE|RESID/i, '')
        .replace(/THUONG.{0,10}/i, '')
        .replace(/[:\-\/]/g, '')
        .trim()

      if (sameLine.length > 3) {
        addressParts.push(sameLine)
      }

      for (let j = i + 1; j < Math.min(i + 4, lines.length); j++) {
        const next = lines[j]

        if (/QUE QUAN|PLACE OF ORIGIN/i.test(next)) break
        if (/\d{2}[\/\-]\d{2}[\/\-]\d{4}/.test(next)) continue

        if (/[A-Za-zÀ-ỹ]/.test(next)) {
          addressParts.push(next)
        }
      }

      if (addressParts.length) {
        result.diaChi = fixCommonOCRErrors(
          addressParts.join(', ')
            .replace(/,\s*,+/g, ', ')
            .replace(/\s{2,}/g, ' ')
            .replace(/^[,.\s]+|[,.\s]+$/g, '')
            .trim()
        )
      }

      break
    }
  }

  return result
}

const fixCommonOCRErrors = (text) => {
  if (!text) return text;

  return text
    .replace(/[\\\/\+\“\”\"\*\@\#\$\%\^\&\_\=\|]/g, '')
    .replace(/ce\s*/gi, '')  // OCR lỗi "cư" -> "ce"
    .replace(/fear\s*/gi, '') // OCR lỗi
    // Chuẩn hóa TDP/Tổ dân phố
    .replace(/\bTDP\b/gi, 'TDP')
    .replace(/\bTO DAN PHO\b/gi, 'TDP')

    // Chuẩn hóa địa danh
    .replace(/Thi\s*xa/gi, 'Thị xã')
    .replace(/Thi\s*tran/gi, 'Thị trấn')
    .replace(/Thanh\s*pho/gi, 'Thành phố')
    .replace(/Quan\s+(\d+)/gi, 'Quận $1')
    .replace(/Huyen/gi, 'Huyện')
    .replace(/Xa\s+([A-Z])/gi, 'Xã $1')
    .replace(/Phuong/gi, 'Phường')

    // Tỉnh thành
    .replace(/Ha\s*Noi/gi, 'Hà Nội')
    .replace(/Ho\s*Chi\s*Minh/gi, 'Hồ Chí Minh')
    .replace(/Da\s*Nang/gi, 'Đà Nẵng')
    .replace(/Hai\s*Phong/gi, 'Hải Phòng')
    .replace(/Can\s*Tho/gi, 'Cần Thơ')
    .replace(/Ha\s*Nam/gi, 'Hà Nam')
    .replace(/Hai\s*Duong/gi, 'Hải Dương')
    .replace(/Hung\s*Yen/gi, 'Hưng Yên')
    .replace(/Nam\s*Dinh/gi, 'Nam Định')
    .replace(/Thai\s*Binh/gi, 'Thái Bình')
    .replace(/Vinh\s*Phuc/gi, 'Vĩnh Phúc')
    .replace(/Bac\s*Ninh/gi, 'Bắc Ninh')
    .replace(/Quang\s*Ninh/gi, 'Quảng Ninh')

    // Đường, số nhà
    .replace(/Duong/gi, 'Đường')
    .replace(/Ngo/gi, 'Ngõ')
    .replace(/So\s+(\d+)/gi, 'Số $1')
    .replace(/\bDuy\s*Tien\b/gi, 'Duy Tiên')
    .replace(/\bYen\s*Bac\b/gi, 'Yên Bắc')
    .replace(/\bVu\s*Xa\b/gi, 'Vũ Xá')

    .replace(/Placoofresdenee/gi, '')
    .replace(/Thuongtru/gi, 'Thường trú')
    .replace(/\bTDP\b/gi, 'TDP')
    .replace(/\bVU\b/gi, 'Vũ')
    .replace(/\bXA\b/gi, 'Xá')
    .replace(/\bYEN\b/gi, 'Yên')
    .replace(/\bDUY\s*TIEN\b/gi, 'Duy Tiên')
    .replace(/Thi\s*xa/gi, 'Thị xã')
    .replace(/Ha\s*Nam/gi, 'Hà Nam')
    .replace(/—|ð|Ll|SÀ/gi, '')
    .replace(/VũXá/gi, 'Vũ Xá')
    .replace(/,\s*,+/g, ', ')
    .replace(/\s{2,}/g, ' ')
    .trim()
    .replace(/\s*,\s*/g, ', ')
    .replace(/\s{2,}/g, ' ')
    .trim();
}
const normalizeOCRText=(text)=> {
  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '') // bỏ dấu tiếng Việt
}

const  parsePassport=(text) =>{
  const result = {}
  const lines = text.split('\n').map(l => l.trim()).filter(l => l.length > 0)

  console.log('All lines:', lines)

  // 1. Loại giấy tờ
  if (/\bP\b/.test(text) && /VNM/.test(text)) {
    result.loaiGiayTo = 1
  }

  // 2. Số hộ chiếu
  const passportMatch = text.match(/\b([B-C]\d{7,8})\b/)
  if (passportMatch) {
    result.soGiayTo = passportMatch[1]
  }

  // 3. Họ tên
// 3. Họ tên
for (let i = 0; i < lines.length; i++) {
  if (/Họ\s*và\s*tên|Full\s*name/i.test(lines[i])) {
    if (i + 1 < lines.length) {
      let nameLine = lines[i + 1]

      nameLine = nameLine.split(/Quốc|Nationality|Date|Sex|Giới|Nơi/i)[0]

      // Loại bỏ ký tự lạ và chỉ giữ chữ
      nameLine = nameLine
        .replace(/[:]/g, '')
        .replace(/[^A-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠƯẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼẾỀỂỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪỬỮỰỲỴỶỸ\s]/gi, '')
        .replace(/\s+/g, ' ')
        .trim()

      // Bỏ các từ 1 ký tự (thường là OCR lỗi)
      const words = nameLine.split(' ').filter(w => w.length > 1)

      if (words.length >= 2 && words.length <= 5) {
        result.hoTen = words.join(' ')
      }
    }
    break
  }
}


  // 4. Quốc tịch
for (let i = 0; i < lines.length; i++) {
  const line = lines[i]
  if (/Qu[cđ]c\s*t[ịi]ch|Nationality/i.test(line)) {
    // lấy toàn bộ chữ sau "Nationality" hoặc "/"
    let match = line.match(/Nationality[:\s]*([A-Z\s\/]+)/i)
    if (match) {
      result.quocTich = match[1].trim()
      break
    } else {
      // fallback: lấy phần text sau "/" nếu có
      const parts = line.split('/')
      if (parts.length >= 2) {
        result.quocTich = parts[1].trim()
        break
      }
    }
  }
}


  // 5. Ngày sinh
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    if (/Ngày\s*sinh.*Date\s*of\s*birth/i.test(line)) {
      const sameLine = line.match(/(\d{2}\s*\/\s*\d{2}\s*\/\s*\d{4})/)
      if (sameLine) {
        result.ngaySinh = sameLine[1].replace(/\s/g, '')
        break
      }
      if (i + 1 < lines.length) {
        const nextLine = lines[i + 1]
        const dateMatch = nextLine.match(/(\d{2}\s*\/\s*\d{2}\s*\/\s*\d{4})/)
        if (dateMatch) {
          result.ngaySinh = dateMatch[1].replace(/\s/g, '')
          break
        }
      }
    }
  }

  // 6. Giới tính
  if (/\/\s*M\b|NAM\b/i.test(text)) {
    result.gioiTinh = 'Nam'
  } else if (/\/\s*F\b|NỮ|NU\b/i.test(text)) {
    result.gioiTinh = 'Nữ'
  }

  // 7. Ngày hết hạn
  for (let i = 0; i < lines.length; i++) {
    if (/Date\s*of\s*expiry|Có\s*giá\s*trị/i.test(lines[i])) {
      const dates = lines[i].match(/\d{2}\s*\/\s*\d{2}\s*\/\s*\d{4}/g)
      if (dates && dates.length >= 1) {
        result.coGiaTriDen = dates[dates.length - 1].replace(/\s/g, '')
        break
      }
      if (i + 1 < lines.length) {
        const nextDates = lines[i + 1].match(/\d{2}\s*\/\s*\d{2}\s*\/\s*\d{4}/g)
        if (nextDates) {
          result.coGiaTriDen = nextDates[nextDates.length - 1].replace(/\s/g, '')
          break
        }
      }
    }
  }

  // MRZ
  const mrzLines = lines.filter(l => {
    const clean = l.replace(/\s/g, '')
    return clean.length >= 40 &&
      /^[A-Z0-9<]+$/.test(clean) &&
      (clean.startsWith('P<') || clean.startsWith('Pq<') || /^[B-C]\d{7}/.test(clean))
  }).map(l => l.replace(/\s/g, ''))

  if (mrzLines.length >= 2) {
    parseMRZ(result, mrzLines)
  }

  return result
}

const parseMRZ=(result, mrzLines)=> {
  if (!mrzLines || mrzLines.length < 2) return

  let line1 = mrzLines[0].replace(/\s/g, '')
  let line2 = mrzLines[1].replace(/\s/g, '')

  line1 = line1.replace(/^Pq</i, 'P<')
  line2 = line2.replace(/^B5/i, 'B3')

  const nameMatch = line1.match(/P<([A-Z]{3})([A-Z<]+)/)
  if (nameMatch && !result.hoTen) {
    const namePart = nameMatch[2]
    const parts = namePart.split('<<')
    if (parts.length >= 2) {
      const surname = parts[0].replace(/</g, ' ').trim()
      const givenNames = parts[1].replace(/</g, ' ').trim()
      result.hoTen = `${surname} ${givenNames}`
    }
  }

  if (!result.soGiayTo) {
    const passportMatch = line2.match(/^([B-C]\d{7})/)
    if (passportMatch) result.soGiayTo = passportMatch[1]
  }

  if (!result.quocTich) {
    const natMatch = line2.match(/^.{10}([A-Z]{3})/)
    if (natMatch && natMatch[1] === 'VNM') {
      result.quocTich = 'VIỆT NAM / VIETNAMESE'
    }
  }

  if (!result.ngaySinh) {
    const dobMatch = line2.match(/^.{13}(\d{2})(\d{2})(\d{2})/)
    if (dobMatch) {
      const yy = parseInt(dobMatch[1])
      const year = yy > 30 ? `19${yy}` : `20${yy}`
      result.ngaySinh = `${dobMatch[3]}/${dobMatch[2]}/${year}`
    }
  }

  if (!result.gioiTinh) {
    const sexMatch = line2.match(/^.{20}([MF])/)
    if (sexMatch) {
      result.gioiTinh = sexMatch[1] === 'M' ? 'Nam' : 'Nữ'
    }
  }

  if (!result.coGiaTriDen) {
    const expMatch = line2.match(/^.{21}(\d{2})(\d{2})(\d{2})/)
    if (expMatch) {
      const yy = parseInt(expMatch[1])
      const year = yy > 30 ? `19${yy}` : `20${yy}`
      result.coGiaTriDen = `${expMatch[3]}/${expMatch[2]}/${year}`
    }
  }
}

</script>

<style scoped>
  .n-message-wrapper {
  z-index: 10000 !important; /* cao hơn modal */
}

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

.container-btn {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  margin-top: 15px;
  justify-content: center;
}

.class-btn {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  white-space: nowrap;
  word-break: normal;
  min-width: 100px;
  height: 45px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  writing-mode: horizontal-tb;
}

.btn-camera {
  background-color: #36AD6A;
  color: #fff;
}


.btn-upload {
  background-color: #36AD6A;
  color: #fff;
  position: relative;
  overflow: hidden;
}



.btn-upload input[type="file"] {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.icon {
  font-size: 20px;
}
</style>
