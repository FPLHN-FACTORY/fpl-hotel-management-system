package com.be.server.core.admin.tag.model.response;


import com.be.server.core.common.base.IsIdentify;

public interface TagResponse extends IsIdentify {

        Integer getRowNumber();
        String getMa();
        String getTen();
        String getMoTa();
        String getStatus();


}
