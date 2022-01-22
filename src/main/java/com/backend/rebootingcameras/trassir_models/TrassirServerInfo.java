package com.backend.rebootingcameras.trassir_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "trassir_server_info")
// класс контейнер для хранения информации о сервере
public class TrassirServerInfo {

    @Id
    @Column(name = "guid")
    private String guid;
    @Basic
    @Column(name = "server_name")
    private String serverName;
    @Basic
    @Column(name = "server_ip")
    private String serverIP;
    @Basic
    @Column(name = "channels_total")
    private Integer channelsTotal;
    @Basic
    @Column(name = "channels_online")
    private Integer channelsOnline;
    @Basic
    @Column(name = "server_status")
    private Integer serverStatus;

    @Basic
    @Column(name = "session_id")
    private String sessionId;
    @Basic
    @Column(name = "lust_update")
    private Date lustUpdate;

    @Basic
    @Column(name = "error_code")
    private String errorCode;


}
