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
public class TrassirChannels {


    private String guid;

    private String error_code;

}
