package com.backend.rebootingcameras.utils;

import com.backend.rebootingcameras.RebootingCamerasApplication;
import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.search.RebootValues;
import lombok.SneakyThrows;

import java.io.*;

/**
 * класс для запуска snmpSet для выполнения перезагрузки по пое
 */
public class RebootChannel implements Runnable {


    private RebootValues rebootValues;
    private boolean wasErrorOnExecuteSnmp;
    private boolean wasErrorOnChangePoeStatus;


    public RebootChannel(RebootValues rebootValues) {
        this.rebootValues = rebootValues;
    }

    public boolean[] executeSnmpSetForReboot() {
        run();
        return new boolean[]{wasErrorOnExecuteSnmp, wasErrorOnChangePoeStatus};
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

        try {
            /* запускаю SnmpSet */
            runSnmp(setCommandOff, "poe_of.bin");

            /* читаю файл */
            openFileReader("C:/Users/romanov-av/Documents/rebooting-cameras/poe_of.bin");

        } catch (IOException e) {
            System.out.println("Поймана во время запуска программы SnmpSet.exe " + e);
            wasErrorOnExecuteSnmp = true;
        }

        try {
            /* запускаю SnmpSet */
            runSnmp(setCommandOn, "poe_on.bin");
            /* читаю файл */
            openFileReader("C:/Users/romanov-av/Documents/rebooting-cameras/poe_on.bin");

        } catch (IOException e) {
            System.out.println("Поймана ошибка повторного открытия файла " + e);
            wasErrorOnExecuteSnmp = true;
        }

    }

    private void openFileReader(String filePath) throws IOException {
        BufferedReader reader =
                new BufferedReader(new FileReader(filePath));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
            content.append(System.lineSeparator());
        }
        String result = content.toString();
        RebootingCamerasApplication.log.debug(result);
        System.out.println(result);
        wasErrorOnChangePoeStatus = !result.contains("OK"); //todo возможно нужно ловить исключения
    }

    private void runSnmp(String setCommand, String fileName) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(PathForRequest.STRING_FOR_RUN_SNMP_SET + setCommand);
        DataOutputStream outputStream = new DataOutputStream(
                new FileOutputStream(fileName)); // создаю поток данных для записи в этот файл
        process.getInputStream().transferTo(outputStream);
        process.destroy();
        wasErrorOnExecuteSnmp = false;
        Thread.sleep(4000);
    }
}
