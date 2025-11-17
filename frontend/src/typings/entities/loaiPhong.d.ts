/// <reference path="../global.d.ts"/>

/** Bảng dữ liệu Loại phòng */
namespace Entity {
  // Kiểu tên loại phòng (ví dụ “Phòng 3 giường”)
  type LoaiPhongType = 'Phòng 3 giường' | 'Phòng 2 giường đơn' | 'Phòng 1 giường đôi 1 giường đơn'

  // Interface mô tả bảng LoaiPhong
  interface LoaiPhong {
    /** ID loại phòng */
    id?: number
    /** Tên loại phòng */
    tenLoai?: LoaiPhongType
    /** Quy định (VD: số người tối thiểu hoặc số giường) */
    quyDinh?: number
    /** Số người tối đa có thể ở */
    toiDa?: number
  }
}
