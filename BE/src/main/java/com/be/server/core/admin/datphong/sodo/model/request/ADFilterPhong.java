package com.be.server.core.admin.datphong.sodo.model.request;

import com.be.server.infrastructure.constant.TinhTrangPhong;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ADFilterPhong {
    private String tenPhong;

    private TinhTrangPhong tinhTrangPhong;

}
