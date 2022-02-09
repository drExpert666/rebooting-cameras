package com.backend.rebootingcameras.trassir_requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties
public class UserRightsBaseRights {

    private String directory;
    private String name;
    private String type;
    private Integer value;

    private String error_code;


}
