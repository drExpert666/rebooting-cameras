package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.utils.TrassirSession;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Controller
public class TrassirController {

    private final static String TRASSIR_MAIN_NAME = "trassir";

    private final static String SESSION_URL_WITH_SDK = "https://" + TRASSIR_MAIN_NAME + ":8080/login?password=12345";

    private TrassirSession trassirSession;

    private RestTemplate restTemplate;

    @Autowired
    public TrassirController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    private ResponseEntity<TrassirSession> openSession(){
        TrassirSession trassirSession = restTemplate.getForObject(SESSION_URL_WITH_SDK, TrassirSession.class);
        this.trassirSession = trassirSession;
        return ResponseEntity.ok(trassirSession);
    }

    @Scheduled(fixedDelay = 3000)
    public void getTrassirSession() {
        if (trassirSession == null) {
            openSession();
            System.out.println(this.trassirSession);
        }
        else {
            System.out.println("Сессия " + this.trassirSession.getSid() + " уже созана");
        }
    }



}
