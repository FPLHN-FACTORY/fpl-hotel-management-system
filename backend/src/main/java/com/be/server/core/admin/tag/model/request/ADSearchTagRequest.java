package com.be.server.core.admin.tag.model.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ADSearchTagRequest extends PageableRequest {
    String maOrTen;
    Integer status;
}
