package com.backend.rebootingcameras.trassir_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor

// класс контейнер для хранения информации о сервере
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


}
