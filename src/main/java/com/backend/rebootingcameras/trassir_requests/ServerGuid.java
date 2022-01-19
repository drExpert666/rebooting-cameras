package com.backend.rebootingcameras.trassir_requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties
public class ServerGuid {

    private String name; // guid сервера
    private String type;
    private String[] subdirs;
    private String[] values;

    private String error_code;

}
