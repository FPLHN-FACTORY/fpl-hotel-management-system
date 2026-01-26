import { defineStore } from 'pinia'
import type { ChonLoaiPhong } from '@/service/api/letan/booking'

interface BookingConfirmationState {
  ngayNhan: number | null
  ngayTra: number | null
  soLuongKhach: number
  danhSachLoaiPhong: ChonLoaiPhong[]
}

export const useBookingStore = defineStore('booking-confirmation', {
  state: (): BookingConfirmationState => ({
    ngayNhan: null,
    ngayTra: null,
    soLuongKhach: 1,
    danhSachLoaiPhong: [],
  }),
  actions: {
    setBookingData(data: {
      ngayNhan: number
      ngayTra: number
      soLuongKhach: number
      danhSachLoaiPhong: ChonLoaiPhong[]
    }) {
      this.ngayNhan = data.ngayNhan
      this.ngayTra = data.ngayTra
      this.soLuongKhach = data.soLuongKhach
      this.danhSachLoaiPhong = data.danhSachLoaiPhong
    },
    clearBookingData() {
      this.ngayNhan = null
      this.ngayTra = null
      this.soLuongKhach = 1
      this.danhSachLoaiPhong = []
    },
  },
  persist: {
    storage: sessionStorage,
  },
})
