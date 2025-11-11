package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.*;
import jdk.jfr.Enabled;

import java.io.Serializable;

@Entity
@Table(name = "phong_tags" )
public class PhongTag extends PrimaryEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "phong_id")
    private Phong phong;

}
