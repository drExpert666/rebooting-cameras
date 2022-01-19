package com.backend.rebootingcameras.trassir_requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties
public class TrassirGuid {

    private String[] subdirs;

    private String error_code;

}
