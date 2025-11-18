package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "tag")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tag extends PrimaryEntity implements Serializable {
    @Lob
    @Column(name = "mo_ta")
    String moTa;

}
