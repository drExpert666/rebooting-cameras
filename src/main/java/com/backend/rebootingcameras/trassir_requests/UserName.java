package com.backend.rebootingcameras.trassir_requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties
public class UserName {

    private String directory;
    private String name;
    private String type;
    private String value;

    private String error_code;
}
