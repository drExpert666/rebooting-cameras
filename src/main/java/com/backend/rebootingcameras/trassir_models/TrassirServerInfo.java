package com.backend.rebootingcameras.trassir_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode

// класс контейнер для хранилища инвормации о сервере
public class TrassirServerInfo {

    private String guid;
    private String serverName;
    private String serverIP;
    private Integer channels_total;
    private Integer channels_online;
    private Integer serverStatus;

    private String sessionId;
    private Date lustUpdate;

    private String error_code;

    public TrassirServerInfo(String guid, String serverName, String serverIP, Integer channels_total, Integer channels_online, Integer serverStatus, String sessionId, Date lustUpdate) {
        this.guid = guid;
        this.serverName = serverName;
        this.serverIP = serverIP;
        this.channels_total = channels_total;
        this.channels_online = channels_online;
        this.serverStatus = serverStatus;
        this.sessionId = sessionId;
        this.lustUpdate = lustUpdate;
    }


}
