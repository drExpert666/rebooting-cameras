package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.search.ChannelSearchValues;
import com.backend.rebootingcameras.service.TrassirChannelService;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // разрешить для этого ресурса получать данные с бэкенда
@RequestMapping("/common") //todo поменять на channel, если класс channel не пригодится
public class CommonChannelController {

    private TrassirController trassirController;

    @Autowired
    public void setChannelService(TrassirChannelService channelService) {
        this.channelService = channelService;
    }

    private TrassirChannelService channelService;

    @Autowired
    public CommonChannelController(TrassirController trassirController) {
        this.trassirController = trassirController;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrassirChannelInfo>> findAll() {
        List<TrassirChannelInfo> channels = channelService.findAll();
        return new ResponseEntity(channels, HttpStatus.OK);
    }

    @PostMapping("/search")
    public ResponseEntity<List<TrassirChannelInfo>> search(@RequestBody ChannelSearchValues searchValues) {

        System.out.println(searchValues);

        /* сохраняю переданные значения */
        String serverId = searchValues.getGuidServer() != null && searchValues.getGuidServer().trim().length() > 0
                ? searchValues.getGuidServer() : null;
        String channelName = searchValues.getName() != null && searchValues.getName().trim().length() > 0
                ? searchValues.getName() : null;
        Integer signal = searchValues.getSignal() != null ? searchValues.getSignal() : null;
        Long switchId = searchValues.getSwitchId() != null ? searchValues.getSwitchId() : null;
        String channelIp = searchValues.getIp() != null && searchValues.getIp().trim().length() > 0
                ? searchValues.getIp() : null;
        Boolean lostChannel = searchValues.getLostChannel();

        // сортировка
        String sortDirection = searchValues.getSortDirection() == null ||
                searchValues.getSortDirection().trim().length() == 0 ||
                !searchValues.getSortDirection().trim().equalsIgnoreCase(PathForRequest.OPTIONAL_SORT_DIRECTION_COLUMN) ?
                PathForRequest.DEFAULT_SORT_DIRECTION_COLUMN : PathForRequest.OPTIONAL_SORT_DIRECTION_COLUMN;

        // направление сортировки
        Sort.Direction direction = sortDirection.equals(PathForRequest.DEFAULT_SORT_DIRECTION_COLUMN)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        String sortColumn = searchValues.getSortColumn() != null ?
                searchValues.getSortColumn() : PathForRequest.DEFAULT_SORT_COLUMN;

        // объект сортировки
        Sort sort = Sort.by(direction, sortColumn);

        // постраничность
        int pageNumber = searchValues.getPageNumber() != null && searchValues.getPageNumber() >= 0 ?
                searchValues.getPageNumber() : 0;
        int pageSize = searchValues.getPageSize() != null && searchValues.getPageSize() > 0 ?
                searchValues.getPageSize() : 10;

        // объект постраничности
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        // результат запроса с постраничным выводом
        Page<TrassirChannelInfo> channelsWithPaginationAndSorting = channelService.findByParams(serverId, null,
                channelName, signal, switchId, channelIp, lostChannel, pageRequest);

        return new ResponseEntity(channelsWithPaginationAndSorting, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<TrassirChannelInfo> update(@RequestBody TrassirChannelInfo channel) {

        System.out.println(channel);

        if (channel.getGuidChannel() == null || channel.getGuidChannel().trim().length() == 0) {
            return new ResponseEntity("id must be fill", HttpStatus.NOT_ACCEPTABLE);
        } else {
            return new ResponseEntity(channelService.updateByChannel(channel), HttpStatus.OK);
        }
    }

}
