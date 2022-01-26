package com.backend.rebootingcameras.utils;

import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.search.RebootValues;
import com.backend.rebootingcameras.service.SwitchService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

/** класс для запуска snmpSet для выполнения перезагрузки по пое */
public class RebootChannel implements Runnable{


    private RebootValues rebootValues;
    private String resultOfReboot;


    public RebootChannel(RebootValues rebootValues) {
        this.rebootValues = rebootValues;
    }

    public String executeSnmpSetForReboot() {
        run();
        return resultOfReboot;
    }


    @SneakyThrows
    @Override
    public void run() {
        String setCommandOff = String.format(PathForRequest.STRING_FOR_REBOOT_CISCO,
                rebootValues.getSwitchIp(), rebootValues.getCameraPort(), PathForRequest.STRING_FOR_REBOOT_OFF);
        String setCommandOn = String.format(PathForRequest.STRING_FOR_REBOOT_CISCO,
                rebootValues.getSwitchIp(), rebootValues.getCameraPort(), PathForRequest.STRING_FOR_REBOOT_ON);

        System.out.println(Thread.currentThread().getName());
        System.out.println(PathForRequest.STRING_FOR_RUN_SNMP_SET + setCommandOff);
        System.out.println(PathForRequest.STRING_FOR_RUN_SNMP_SET + setCommandOn);

        Process process = Runtime.getRuntime().exec(PathForRequest.STRING_FOR_RUN_SNMP_SET + setCommandOff);
//        process.getInputStream();
        DataOutputStream outputStream = new DataOutputStream(
                new FileOutputStream("poe_of.bin")); // создаю поток данных для записи в этот файл

        process.getInputStream().transferTo(outputStream);
        process.destroy();
        Thread.sleep(5000);

        Process process2 = Runtime.getRuntime().exec(PathForRequest.STRING_FOR_RUN_SNMP_SET + setCommandOn);
        process2.getInputStream();
        DataOutputStream outputStream2 = new DataOutputStream(new FileOutputStream("poe_on.bin"));
        process2.getInputStream().transferTo(outputStream2);
        Thread.sleep(5000);
        process2.destroy();
        //todo написать метод чтения информации из файлов poe_of.bin, poe_on.bin и вернуть результат (ОК? не ок?)
        resultOfReboot = "OKEY";

    }
}
