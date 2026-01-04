package com.be.server.repository;

import com.be.server.core.admin.datphong.trangthaiphong.model.response.DSPhongDaDatCombox;
import com.be.server.entity.DatPhong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatPhongRepository extends JpaRepository<DatPhong, String> {
    @Query("select dp.id as value,\n" +
            "dp.tenPhong as label\n" +
            "from DatPhong dp\n" +
            "where dp.trangThaiPhong=1")
    List<DSPhongDaDatCombox> getDataComboboxDatPhong();
    boolean existsById(String Id);
}
