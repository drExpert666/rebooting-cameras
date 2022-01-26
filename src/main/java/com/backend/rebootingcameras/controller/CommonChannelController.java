package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.search.ChannelSearchValues;
import com.backend.rebootingcameras.service.TrassirChannelService;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // разрешить для этого ресурса получать данные с бэкенда
@RequestMapping("/common") //todo поменять на channel, если класс channel не пригодится
public class CommonChannelController {

    private TrassirController trassirController;

    @Autowired
    public void setChannelService(TrassirChannelService channelService) {
        this.channelService = channelService;
    }

    private TrassirChannelService channelService;

    @Autowired
    public CommonChannelController(TrassirController trassirController) {
        this.trassirController = trassirController;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrassirChannelInfo>> findAll() {
        List<TrassirChannelInfo> channels = channelService.findAll();
        return new ResponseEntity(channels, HttpStatus.OK);
    }

    @PostMapping("/search")
    public ResponseEntity<List<TrassirChannelInfo>> search(@RequestBody ChannelSearchValues searchValues) {
        String serverId = searchValues.getGuidServer();
        String channelName = searchValues.getName();
        Integer signal = searchValues.getSignal();
        Long switchId = searchValues.getSwitchId();
        List<TrassirChannelInfo> channels = channelService.findByParams(serverId, null, channelName, signal, switchId);
        return new ResponseEntity(channels, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<TrassirChannelInfo> update(@RequestBody TrassirChannelInfo channel) {

        if (channel.getGuidChannel() == null || channel.getGuidChannel().trim().length() == 0) {
            return new ResponseEntity("id must be fill", HttpStatus.NOT_ACCEPTABLE);
        }
        else {
            return new ResponseEntity(channelService.updateByChannel(channel), HttpStatus.OK);
        }
    }



}
