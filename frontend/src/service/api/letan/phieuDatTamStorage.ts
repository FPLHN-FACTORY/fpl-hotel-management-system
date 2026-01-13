const STORAGE_KEY = 'phieu_dat_tam_list'
const EXPIRY_TIME = 24 * 60 * 60 * 1000

export interface PhieuDatTamLocal {
  id: string
  sessionId: string
  checkInDate: number
  checkOutDate: number
  soLuongKhach: number
  idKhachHang: string | null
  tenKhachHang: string | null
  ghiChu: string | null
  nhanNgay: boolean
  tienKhachTra: number | null
  tongTien: number
  tienThua: number
  congNo: number
  danhSachPhong: Array<{
    idPhong: string
    maPhong: string
    tenPhong: string
    tenLoaiPhong: string
    tang: number
    gia: number
    soNgay: number
    thanhTien: number
  }>
  isFromRoomClick: boolean
  createdAt: number
  // Thêm field để track flow step
  currentStep: 'SELECT_ROOM' | 'CUSTOMER_INFO' | 'PAYMENT_INFO' | 'READY_TO_CONFIRM'
}

class PhieuDatTamStorage {

  getAllPhieuDatTam(): PhieuDatTamLocal[] {
    this.cleanupExpired()
    const data = localStorage.getItem(STORAGE_KEY)
    if (!data) return []

    try {
      return JSON.parse(data)
    } catch (error) {
      console.error('Error parsing phieu dat tam:', error)
      return []
    }
  }

  getPhieuDatTam(sessionId: string): PhieuDatTamLocal | null {
    const allPhieu = this.getAllPhieuDatTam()
    const result = allPhieu.find(p => p.sessionId === sessionId) || null
    return result
  }

  savePhieuDatTam(phieu: Omit<PhieuDatTamLocal, 'id' | 'createdAt' | 'sessionId'> & { sessionId?: string }): PhieuDatTamLocal {

    const allPhieu = this.getAllPhieuDatTam()

    if (phieu.sessionId) {

      const existingIndex = allPhieu.findIndex(p => p.sessionId === phieu.sessionId)
      if (existingIndex !== -1) {

        const updated = {
          ...allPhieu[existingIndex],
          ...phieu,
          sessionId: phieu.sessionId,
        }
        allPhieu[existingIndex] = updated
        this.saveToStorage(allPhieu)


        return updated
      }
    }

    const generatedSessionId = phieu.sessionId || this.generateSessionId()

    const newPhieu: PhieuDatTamLocal = {
      id: this.generateId(),
      createdAt: Date.now(),
      ...phieu,
      sessionId: generatedSessionId,
    }


    allPhieu.unshift(newPhieu)
    this.saveToStorage(allPhieu)
    return newPhieu
  }

  deletePhieuDatTam(sessionId: string): boolean {
    const allPhieu = this.getAllPhieuDatTam()
    const filtered = allPhieu.filter(p => p.sessionId !== sessionId)

    if (filtered.length === allPhieu.length) {
      return false
    }

    this.saveToStorage(filtered)
    return true
  }

  cleanupExpired(): void {
    const now = Date.now()
    const allPhieu = this.getRawData()
    const filtered = allPhieu.filter(p => (now - p.createdAt) < EXPIRY_TIME)

    if (filtered.length !== allPhieu.length) {
      this.saveToStorage(filtered)
    }
  }

  clearAll(): void {
    localStorage.removeItem(STORAGE_KEY)
  }

  determineNextStep(phieu: PhieuDatTamLocal): 'CUSTOMER_INFO' | 'PAYMENT_INFO' | 'CONFIRM' {

    if (!phieu.idKhachHang) {
      return 'CUSTOMER_INFO'
    }

    if (phieu.tienKhachTra === null || phieu.tienKhachTra === undefined) {
      return 'PAYMENT_INFO'
    }

    return 'CONFIRM'
  }

  private getRawData(): PhieuDatTamLocal[] {
    const data = localStorage.getItem(STORAGE_KEY)
    if (!data) return []
    try {
      return JSON.parse(data)
    } catch {
      return []
    }
  }

  private saveToStorage(data: PhieuDatTamLocal[]): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  }

  private generateId(): string {
    return `phieu_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  private generateSessionId(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  countPending(): number {
    return this.getAllPhieuDatTam().length
  }
}

export const phieuDatTamStorage = new PhieuDatTamStorage()

phieuDatTamStorage.cleanupExpired()
setInterval(() => {
  phieuDatTamStorage.cleanupExpired()
}, 5 * 60 * 1000)
