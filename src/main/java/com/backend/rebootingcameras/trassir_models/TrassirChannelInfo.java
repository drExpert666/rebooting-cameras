package com.backend.rebootingcameras.trassir_models;

import com.backend.rebootingcameras.entity.Switch;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trassir_channel_info")
public class TrassirChannelInfo {

    @ManyToOne
    @JoinColumn(name = "guid_server", referencedColumnName = "guid")
    private TrassirServerInfo guidServer; // id сервера

    @Id
    @Column(name = "guid_channel")
    private String guidChannel; // id канала
    @Basic
    @Column(name = "name")
    private String name; // имя канала
    @Basic
    @Column(name = "signal")
    private Integer signal; // состояние канала

    @Basic
    @Column(name = "guid_ip_device")
    private String guidIpDevice; // id ip девайса
    @Basic
    @Column(name = "ip")
    private String ip; // ip адрес девайса
    @Basic
    @Column(name = "model")
    private String model; // модель устройства

    @Basic
    @Column(name = "lust_update")
    private Date lustUpdate; // последнее обновление информации (когда был последний запрос);

    @Basic
    @Column(name = "poe_injector")
    private Boolean poeInjector;

    @ManyToOne
    @JoinColumn(name = "switch_id", referencedColumnName = "id")
    private Switch switchId;

    @Basic
    @Column(name = "port")
    private Integer port; // номер порта коммутатора

}
