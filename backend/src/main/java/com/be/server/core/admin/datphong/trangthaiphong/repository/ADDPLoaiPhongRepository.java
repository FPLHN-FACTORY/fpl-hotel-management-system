package com.be.server.core.admin.datphong.trangthaiphong.repository;

import com.be.server.core.admin.datphong.trangthaiphong.model.response.LoaiPhongCombobox;
import com.be.server.repository.LoaiPhongRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ADDPLoaiPhongRepository extends LoaiPhongRepository {

    @Query(value = """
    SELECT 
        lp.id as value,
        lp.ten as label
    FROM LoaiPhong lp
    """)
    List<LoaiPhongCombobox> getDataCombobox();

}
