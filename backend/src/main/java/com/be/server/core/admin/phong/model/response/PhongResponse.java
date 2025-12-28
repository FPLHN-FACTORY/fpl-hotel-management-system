package com.be.server.core.admin.phong.model.response;

import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhongResponse implements Serializable {
    private String id;
    private String ma;
    private String ten;
    private BigDecimal price;
    private Integer tang;
    private String loaiPhong;
    private Integer sucChua;
    private Integer soNguoiQuyDinh;
    private Integer soGiuongDon;
    private Integer soGiuongDoi;
    private TrangThaiHoatDong trangThaiHoatDong;
    private List<TagInfo> tags;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagInfo {
        private String id;
        private String ma;
        private String ten;
        private String mau;

        public TagInfo(String id, String ma, String ten) {
            this.id = id;
            this.ma = ma;
            this.ten = ten;
            this.mau = null;
        }
    }

    public String getCapacityRange() {
        if (soNguoiQuyDinh != null && sucChua != null) {
            return soNguoiQuyDinh + "~" + sucChua;
        } else if (sucChua != null) {
            return String.valueOf(sucChua);
        }
        return "-";
    }
}