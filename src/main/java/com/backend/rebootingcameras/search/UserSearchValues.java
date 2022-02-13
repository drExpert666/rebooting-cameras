package com.backend.rebootingcameras.search;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserSearchValues {

    private String channelGuid;
    private List<String> usersFromChannel;

}
