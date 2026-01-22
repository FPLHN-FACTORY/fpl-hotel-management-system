package com.be.server.core.admin.doanluutru.modal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO để thêm khách trong quá trình lưu trú
 * Kế thừa/Mở rộng yêu cầu thêm thành viên với các trường bắt buộc để gán phòng
 */
@Getter
@Setter
public class AddGuestDuringStayRequest {
    
    @NotBlank(message = "ID đoàn lưu trú không được để trống")
    private String idDoanLuuTru;
    
    @NotBlank(message = "ID phòng không được để trống")
    private String idPhong;
    
    // Guest information - all required
    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;
    
    @NotNull(message = "Giới tính không được để trống")
    private Integer gioiTinh;
    
    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate ngaySinh;
    
    @NotNull(message = "Loại giấy tờ không được để trống")
    private Integer loaiGiayTo;
    
    @NotBlank(message = "Số giấy tờ không được để trống")
    private String soGiayTo;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;
    
    private String email;
    private String diaChi;
    private String quocTich;
    
    // Ghi chú thêm
    private String ghiChu;
    
    // Cờ để sử dụng khách hàng hiện tại nếu tìm thấy
    private Boolean confirmUseOld;
}
