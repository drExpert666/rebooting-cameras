package com.backend.rebootingcameras.trassir_requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties
public class ChannelStatus {

    private String directory;
    private String name;
    private String type;
    private Integer value;

    private String error_code;

}
