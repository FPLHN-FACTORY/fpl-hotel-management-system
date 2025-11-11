/// <reference path="../global.d.ts"/>

/** Bảng dữ liệu Phòng */
namespace Entity {
  interface Room {
    /** ID phòng */
    id?: number

    /** Mã phòng (định danh duy nhất) */
    maPhong?: string

    /** Tên phòng */
    tenPhong?: string

    tang?: number

    /** Loại phòng - tham chiếu tới Entity.LoaiPhong */
    loaiPhong?: LoaiPhong

    /** Giá phòng */
    gia?: number

    /** Sức chứa (số người tối đa) - lấy từ LoaiPhong.toiDa */
    sucChua?: number

    /** Trạng thái: 0 = Trống, 1 = Đã đặt, 2 = Đang sử dụng */
    trangThai?: string
  }
}
