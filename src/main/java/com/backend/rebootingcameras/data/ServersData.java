package com.backend.rebootingcameras.data;

import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class ServersData {

    private ArrayList<TrassirServerInfo> servers;

    public ServersData() {
        fillServers();
    }

    private void fillServers() {
        servers = new ArrayList<>();
        servers.add(new TrassirServerInfo("gZZKuo60", null, "192.168.98.1",
                null, null, null, null, null));
        servers.add(new TrassirServerInfo("kKAZPSPI", null, "192.168.98.4",
                null, null, null, null, null));
    }

}
