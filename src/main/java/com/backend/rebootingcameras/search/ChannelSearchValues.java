package com.backend.rebootingcameras.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChannelSearchValues {
    private String guidServer;
    private String guidChannel;
    private String name;
}
