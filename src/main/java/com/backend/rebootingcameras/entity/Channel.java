package com.backend.rebootingcameras.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "trassir_channel_info")
public class Channel {

    @Id
    @Column(name = "guid_channel")
    private String guidChannel;

    @Basic
    @Column(name = "poe_injector")
    private Boolean poeInjector;

    @ManyToOne
    @JoinColumn(name = "switch_id", referencedColumnName = "id")
    private Switch switchId;

}
