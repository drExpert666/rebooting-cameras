package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.entity.Channel;
import com.backend.rebootingcameras.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/channel")
public class ChannelController {

    private ChannelService channelService;

    @Autowired
    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }


    @PutMapping("/update")
    public ResponseEntity<Channel> updateChannel (@RequestBody Channel channel) {
        channelService.updateChannel(channel);
        return new ResponseEntity<>(channel, HttpStatus.OK);
    }


}
