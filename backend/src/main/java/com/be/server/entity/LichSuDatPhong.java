package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "lich_su_dat_phong")
public class LichSuDatPhong extends PrimaryEntity implements Serializable{
    @ManyToOne
    @JoinColumn(name = "id_dat_phong")
    private DatPhong datPhong;

    @ManyToOne
    @JoinColumn(name = "id_le_tan")
    private LeTan leTan;
}
