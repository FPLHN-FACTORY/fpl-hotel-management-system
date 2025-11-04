package com.be.server.core.admin.phong.model.response;

import com.be.server.infrastructure.constant.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeTanResponse {
    private String datPhongId;
    private String hoTen;
    private String maPhong;
    private String tenPhong;
    private BigDecimal giaHienTai;
    private LocalDateTime thoiGianDat;
    private RoomStatus trangThaiPhong;
    private List<String> dichVuPhatSinh;
    private BigDecimal tongTienPhatSinh;
    private LocalDateTime thoiGianCheckIn;
    private LocalDateTime thoiGianCheckOut;

    public LeTanResponse(String datPhongId,
                         String hoTen,
                         String maPhong,
                         String tenPhong,
                         LocalDateTime thoiGianDat,
                         BigDecimal giaHienTai,
                         RoomStatus trangThaiPhong,
                         LocalDateTime thoiGianCheckIn,
                         LocalDateTime thoiGianCheckOut){
        this.datPhongId = datPhongId;
        this.hoTen = hoTen;
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.thoiGianDat = thoiGianDat;
        this.giaHienTai = giaHienTai;
        this.trangThaiPhong = trangThaiPhong;
        this.thoiGianCheckIn = thoiGianCheckIn;
        this.thoiGianCheckOut = thoiGianCheckOut;
    }
}
