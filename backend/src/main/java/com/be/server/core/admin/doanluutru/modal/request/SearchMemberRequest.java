package com.be.server.core.admin.doanluutru.modal.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchMemberRequest extends PageableRequest {
    String hoTen;
    Integer loaiGiayTo;
    String soGiayTo;
}
