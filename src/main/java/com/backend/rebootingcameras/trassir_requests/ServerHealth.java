package com.backend.rebootingcameras.trassir_requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties
public class ServerHealth {

    private Integer network;
    private Integer channels_total;
    private Integer channels_online;

    private String error_code;

}
