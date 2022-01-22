package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.service.TrassirServerService;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/server")
@CrossOrigin(origins = "http://localhost:4200") // разрешить для этого ресурса получать данные с бэкенда)
public class ServerController {

    private TrassirServerService serverService;

    @Autowired
    public ServerController(TrassirServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrassirServerInfo>> findAll() {
        return new ResponseEntity<>(serverService.findAll(), HttpStatus.OK);
    }


}
