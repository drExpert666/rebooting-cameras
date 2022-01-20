package com.backend.rebootingcameras.trassir_models;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Table(name = "trassir_channel_info")
public class TrassirChannelInfo {

    @Basic
    @Column(name = "guid_server")
    private String guidServer; // id канала

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



}
