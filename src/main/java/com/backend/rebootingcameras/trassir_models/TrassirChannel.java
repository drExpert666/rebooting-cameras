package com.backend.rebootingcameras.trassir_models;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class TrassirChannel {

    private String guidServer; // id канала

    private String guidChannel; // id канала
    private String name; // имя канала
    private Integer signal; // состояние канала

    private String guidIpDevice; // id ip девайса
    private String ip; // ip адрес девайса
    private String model; // модель устройства

    private Date lustUpdate; // последнее обновление информации (когда был последний запрос);



}
