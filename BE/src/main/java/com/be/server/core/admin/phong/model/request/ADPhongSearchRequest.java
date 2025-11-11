package com.be.server.core.admin.phong.model.request;

import com.be.server.core.common.base.PageableRequest;
import com.be.server.entity.LoaiPhong;
import com.be.server.infrastructure.constant.RoomStatus;
import com.be.server.infrastructure.constant.TrangThaiPhong;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ADPhongSearchRequest extends PageableRequest {


    private String tuKhoa;

    private Integer tang;

    private String loaiPhongId;

    private TrangThaiPhong trangThaiPhong;

    private BigDecimal giaMin;

    private BigDecimal giaMax;

}