package com.backend.rebootingcameras.trassir_requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties
public class ChannelName {

    private String directory;
    private String name;
    private String type;
    private String value;

    private String error_code;

}
