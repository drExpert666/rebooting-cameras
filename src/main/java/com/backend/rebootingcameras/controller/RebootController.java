package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.search.RebootValues;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reboot")
@CrossOrigin(origins = "http://localhost:4200") // разрешить для этого ресурса получать данные с бэкенда)
public class RebootController {

    @PostMapping("/values")
    public ResponseEntity<RebootValues> rebootCamera(@RequestBody RebootValues rebootValues) {
        System.out.println(rebootValues);
        return new ResponseEntity(rebootValues, HttpStatus.OK);
    }

}
