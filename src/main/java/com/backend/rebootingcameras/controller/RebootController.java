package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.entity.Switch;
import com.backend.rebootingcameras.search.RebootValues;
import com.backend.rebootingcameras.service.SwitchService;
import com.backend.rebootingcameras.utils.RebootChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/reboot")
@CrossOrigin(origins = "http://localhost:4200") // разрешить для этого ресурса получать данные с бэкенда)
@Slf4j
public class RebootController {

    private SwitchService switchService;

    @Autowired
    public void setSwitchService(SwitchService switchService) {
        this.switchService = switchService;
    }


    @PostMapping("/values")
    public ResponseEntity<RebootValues> rebootCamera(@RequestBody RebootValues rebootValues) {


        System.out.println(rebootValues);

        // присваиваю значение порта в зависимости от типа коммутатора
        Integer portNumberForReboot = getSwitchIpAndPortNumberForReboot(rebootValues);

        // если из метода выше вернулось значение, то выполняем команду по перезагрузке
        if (portNumberForReboot != null) {
            System.out.println(portNumberForReboot.toString());
            RebootValues newRebootValues = new RebootValues(rebootValues.getSwitchIp(), portNumberForReboot.toString());
            System.out.println(newRebootValues);

            // передаём в параметрах значения для перезагрузки
            RebootChannel rebootChannel = new RebootChannel(newRebootValues);
            // вызываем метод перезагрузки
            boolean[] result =  rebootChannel.executeSnmpSetForReboot();
            if (!result[0] & !result[1]) {
                rebootValues.setSwitchIp("OK");
            }
            else {
                rebootValues.setSwitchIp("ERROR");
            }
            System.out.println(Arrays.toString(result));

            log.info(Arrays.toString(result));
            return ResponseEntity.ok(rebootValues);
        }

        else {
            return new ResponseEntity(new RebootValues("PORT_NOT_FOUND", null), HttpStatus.OK);
        }

    }

    private Integer getSwitchIpAndPortNumberForReboot(RebootValues rebootValues) {
        // получаю коммутатор по переданному ip
        Switch tmpSwitch = switchService.findByIp(rebootValues.getSwitchIp());
        if (tmpSwitch == null) {
            return null;
        }
        String tmpSwitchName;
        Integer newPortNumber;
        // если имя коммутатора было заполнено
        if (tmpSwitch.getName() != null) {
            tmpSwitchName = tmpSwitch.getName();
        } else {
            return null;
        }

        // перевожу номер порта из текста в интеджер
        newPortNumber = Integer.parseInt(rebootValues.getCameraPort());

        // проверяю в свитч-кейсе на соответсвие модели коммутатора
        switch (tmpSwitchName) {
            case (PathForRequest.SWITCH_MODEL_CISCO_SG200):
            case (PathForRequest.SWITCH_MODEL_CISCO_SG300):
                newPortNumber += 48;
                break;
            case (PathForRequest.SWITCH_MODEL_CISCO_SFE2000P):
                break;
            default:
                newPortNumber = null;
        }

        return (newPortNumber);
    }

}
