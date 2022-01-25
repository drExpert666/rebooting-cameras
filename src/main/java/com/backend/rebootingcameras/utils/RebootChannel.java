package com.backend.rebootingcameras.utils;

import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.search.RebootValues;
import lombok.SneakyThrows;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RebootChannel implements Runnable{


    private RebootValues rebootValues;

    public RebootChannel(RebootValues rebootValues) {
        this.rebootValues = rebootValues;
    }

    public void rebootCamByPOE() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new RebootChannel(rebootValues));
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println(Thread.currentThread().getName());


    }

    @SneakyThrows
    @Override
    public void run() {
        String setCommandOff = String.format(PathForRequest.STRING_FOR_REBOOT_CISCO, rebootValues.getSwitchIp(), rebootValues.getCameraPort(), '2');

        String setCommandOn = String.format(PathForRequest.STRING_FOR_REBOOT_CISCO, rebootValues.getSwitchIp(), rebootValues.getCameraPort(), '1');

        System.out.println(Thread.currentThread().getName());

        System.out.println(PathForRequest.STRING_FOR_RUN_SNMP_SET + setCommandOff);

        System.out.println(PathForRequest.STRING_FOR_RUN_SNMP_SET + setCommandOn);

        Process process = Runtime.getRuntime().exec(PathForRequest.STRING_FOR_RUN_SNMP_SET + setCommandOff);
        process.getInputStream();
        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream("poe_of.bin"));
        process.getInputStream().transferTo(outputStream);
        process.destroy();
        Thread.sleep(5000);

        Process process2 = Runtime.getRuntime().exec(PathForRequest.STRING_FOR_RUN_SNMP_SET + setCommandOn);
        process2.getInputStream();
        DataOutputStream outputStream2 = new DataOutputStream(new FileOutputStream("poe_on.bin"));
        process2.getInputStream().transferTo(outputStream2);
        Thread.sleep(5000);
        process2.destroy();

    }
}
