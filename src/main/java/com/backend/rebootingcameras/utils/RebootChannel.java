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
        /* win10 */
//        String setCommandOff = String.format(PathForRequest.STRING_FOR_REBOOT_CISCO,
//                rebootValues.getSwitchIp(), rebootValues.getCameraPort(), PathForRequest.STRING_FOR_REBOOT_OFF);
//        String setCommandOn = String.format(PathForRequest.STRING_FOR_REBOOT_CISCO,
//                rebootValues.getSwitchIp(), rebootValues.getCameraPort(), PathForRequest.STRING_FOR_REBOOT_ON);

        /* ubuntu */
        String setCommandOff = String.format(PathForRequest.STRING_FOR_REBOOT_CISCO_UBUNTU,
                rebootValues.getSwitchIp(), rebootValues.getCameraPort(), PathForRequest.STRING_FOR_REBOOT_OFF);
        String setCommandOn = String.format(PathForRequest.STRING_FOR_REBOOT_CISCO_UBUNTU,
                rebootValues.getSwitchIp(), rebootValues.getCameraPort(), PathForRequest.STRING_FOR_REBOOT_ON);
        System.out.println(Thread.currentThread().getName());
        System.out.println(setCommandOff);
        System.out.println(setCommandOn);
        try {
            /* запускаю SnmpSet */
            runSnmp(setCommandOff);
            Thread.sleep(2000);

            /* читаю файл */
//            openFileReader("C:/Users/romanov-av/Documents/rebooting-cameras/poe_of.bin");

        } catch (IOException e) {
            System.out.println("Поймана во время запуска программы SnmpSet.exe " + e);
            wasErrorOnExecuteSnmp = true;
        }

        try {
            /* запускаю SnmpSet */
            runSnmp(setCommandOn);
            /* читаю файл */
//            openFileReader("C:/Users/romanov-av/Documents/rebooting-cameras/poe_on.bin");

        } catch (IOException e) {
            System.out.println("Поймана ошибка повторного открытия файла " + e);
            wasErrorOnExecuteSnmp = true;
        }

    }

    //todo убрать лишний метод
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
        wasErrorOnChangePoeStatus = !result.contains("OK");
    }

    private void runSnmp(String setCommand) throws IOException{
        Process process = Runtime.getRuntime().exec(
//                PathForRequest.STRING_FOR_RUN_SNMP_SET + /* win10 */
                setCommand);

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder builder = new StringBuilder();
        String line = "";
        while((line = reader.readLine()) != null) {
            builder.append(line);
        }
        System.out.println(builder.toString());
        wasErrorOnChangePoeStatus = !builder.toString().contains("INTEGER");
        try {
            process.waitFor();
            wasErrorOnExecuteSnmp = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            wasErrorOnExecuteSnmp = true;
        }


        /* win10 */
//        DataOutputStream outputStream = new DataOutputStream(
//                new FileOutputStream(fileName)); // создаю поток данных для записи в этот файл
//        process.getInputStream().transferTo(outputStream);
//        process.destroy();
//        wasErrorOnExecuteSnmp = false;
//        Thread.sleep(4000);
    }
}
