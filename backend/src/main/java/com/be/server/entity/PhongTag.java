package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "phong_tags" )
public class PhongTag extends PrimaryEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "phong_id")
    private Phong phong;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

}
