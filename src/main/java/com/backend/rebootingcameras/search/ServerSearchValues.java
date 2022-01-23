package com.backend.rebootingcameras.search;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonAutoDetect
public class ServerSearchValues {
    private String serverName;
}
