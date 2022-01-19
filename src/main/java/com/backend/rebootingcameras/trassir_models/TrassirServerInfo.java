package com.backend.rebootingcameras.trassir_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties

// класс контейнер для хранилища инвормации о сервере
public class TrassirServerInfo {

    private String value;
    private String guid;
    private String serverName;
    private Integer channels_total;
    private Integer channels_online;
    private Integer network;

    private String error_code;

}
