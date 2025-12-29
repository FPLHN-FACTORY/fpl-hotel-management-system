package com.be.server.core.admin.lichdatphong.datphong.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckPhongTrongRequest {

    Long ngayNhan;

    Long ngayTra;

    Integer soLuongKhach;

}
