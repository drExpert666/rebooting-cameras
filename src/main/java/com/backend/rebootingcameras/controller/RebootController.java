package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.search.RebootValues;
import com.backend.rebootingcameras.utils.RebootChannel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/reboot")
@CrossOrigin(origins = "http://localhost:4200") // разрешить для этого ресурса получать данные с бэкенда)
public class RebootController {

    HashMap<String, String> oids84 = new HashMap<>();


    @PostMapping("/values")
    public ResponseEntity<RebootValues> rebootCamera(@RequestBody RebootValues rebootValues) {
        oids84.put("1", "49");
        oids84.put("2", "50");
        oids84.put("3", "51");
        oids84.put("4", "52");
        oids84.put("5", "53");
        oids84.put("6", "54");
        oids84.put("13", "61");
        oids84.put("14", "62");
        oids84.put("15", "63");
        oids84.put("16", "64");
        oids84.put("17", "65");
        oids84.put("18", "66");
        System.out.println(rebootValues);
        if (rebootValues.getSwitchIp().equals("192.168.254.84")) {
           rebootValues.setCameraPort(oids84.get(rebootValues.getCameraPort()));
            RebootChannel rebootChannel = new RebootChannel(rebootValues);
            rebootChannel.run();
            return new ResponseEntity(rebootValues, HttpStatus.OK);
        } else {
            return new ResponseEntity(new RebootValues(null, null), HttpStatus.OK);
        }


    }

}
