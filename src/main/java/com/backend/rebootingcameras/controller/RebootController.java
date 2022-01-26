package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.entity.Switch;
import com.backend.rebootingcameras.search.RebootValues;
import com.backend.rebootingcameras.service.SwitchService;
import com.backend.rebootingcameras.utils.RebootChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;

@RestController
@RequestMapping("/reboot")
@CrossOrigin(origins = "http://localhost:4200") // разрешить для этого ресурса получать данные с бэкенда)
public class RebootController {

    private SwitchService switchService;

    @Autowired
    public void setSwitchService(SwitchService switchService) {
        this.switchService = switchService;
    }


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

        Integer portNumberForReboot = getSwitchIpAndPortNumberForReboot(rebootValues);
        if (portNumberForReboot != null) {
            System.out.println(portNumberForReboot.toString());
            RebootValues newRebootValues = new RebootValues(rebootValues.getSwitchIp(), portNumberForReboot.toString());
            System.out.println(newRebootValues);

            RebootChannel rebootChannel = new RebootChannel(newRebootValues);
            rebootChannel.run();
            return ResponseEntity.ok(rebootValues);
        }

//        if (rebootValues.getSwitchIp().equals("192.168.254.84")) {
//            rebootValues.setCameraPort(oids84.get(rebootValues.getCameraPort()));
//            RebootChannel rebootChannel = new RebootChannel(rebootValues);
//            rebootChannel.run();
//            return new ResponseEntity(rebootValues, HttpStatus.OK);
//        } else {
//            return new ResponseEntity(new RebootValues(null, null), HttpStatus.OK);
//        }
        else {
            return ResponseEntity.ok(new RebootValues(null, null));
        }

    }

    private Integer getSwitchIpAndPortNumberForReboot(RebootValues rebootValues) {
        Switch tmpSwitch = switchService.findByIp(rebootValues.getSwitchIp());
        if (tmpSwitch == null) {
            return null;
        }
        String tmpSwitchName;
        int newPortNumber;
        if (tmpSwitch.getName() != null) {
            tmpSwitchName = tmpSwitch.getName();
        } else {
            return null;
        }

        newPortNumber = Integer.parseInt(rebootValues.getCameraPort());

        switch (tmpSwitchName) {
            case (PathForRequest.SWITCH_MODEL_CISCO_SG200):
            case (PathForRequest.SWITCH_MODEL_CISCO_SG300):
                newPortNumber = newPortNumber + 48;
                break;
            case (PathForRequest.SWITCH_MODEL_CISCO_SFE2000P):
                break;
        }

        return (newPortNumber);
    }

}
