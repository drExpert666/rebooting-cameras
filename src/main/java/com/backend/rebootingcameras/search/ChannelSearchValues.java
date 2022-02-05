package com.backend.rebootingcameras.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChannelSearchValues {

    // поля для поиска
    private String guidServer;
    private String guidChannel;
    private String name;
    private Integer signal;
    private Long switchId;
    private String ip;

    private Boolean lostChannel;

    // постарничность
    private Integer pageNumber;
    private Integer pageSize;

    // сортировка
    private String sortColumn;
    private String sortDirection;

}
