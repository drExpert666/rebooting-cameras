package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.entity.Channel;
import com.backend.rebootingcameras.trassir_models.CommonChannel;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // разрешить для этого ресурса получать данные с бэкенда
@RequestMapping("/common")
public class CommonChannelController {

    private TrassirController trassirController;

    @Autowired
    public CommonChannelController(TrassirController trassirController) {
        this.trassirController = trassirController;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrassirChannelInfo>> findAll() {
        System.out.println("Пришёл запрос от http://localhost:4200");
        List<TrassirChannelInfo> channels = trassirController.findAllCameras();
        System.out.println(channels);
        return new ResponseEntity(channels, HttpStatus.OK);
    }
}
