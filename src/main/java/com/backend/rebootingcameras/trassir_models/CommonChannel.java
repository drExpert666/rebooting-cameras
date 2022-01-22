package com.backend.rebootingcameras.trassir_models;

import com.backend.rebootingcameras.entity.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommonChannel {

    private TrassirChannelInfo trassirChannelInfo;
    private Channel channel;

}
