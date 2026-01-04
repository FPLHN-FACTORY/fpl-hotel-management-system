package com.be.server.core.admin.khachhang.repository;

import com.be.server.core.admin.khachhang.model.response.LoaiKhachHangCombobox;


import com.be.server.repository.LoaiKhachHangRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ADLoaiKhachHangRepository extends LoaiKhachHangRepository {
    @Query(value = "select lkh.id as value,lkh.ten_loai as label\n" +
            "from loai_khach_hang lkh\n" +
            "where lkh.status=0\n" , nativeQuery = true)
    List<LoaiKhachHangCombobox> getLoaiKhachHangCombobox();
}
