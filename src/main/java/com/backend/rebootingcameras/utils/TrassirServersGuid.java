package com.backend.rebootingcameras.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties
public class TrassirServersGuid {

    private String[] subdirs;

    private String error_code;

}
