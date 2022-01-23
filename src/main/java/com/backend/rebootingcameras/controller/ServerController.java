package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.search.ServerSearchValues;
import com.backend.rebootingcameras.service.TrassirServerService;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/search")
    public ResponseEntity<List<TrassirServerInfo>> findByParams(@RequestBody ServerSearchValues searchValues) {
        System.out.println(searchValues);
        return new ResponseEntity<>(serverService.findByParams(searchValues.getServerName()), HttpStatus.OK);
    }

}
