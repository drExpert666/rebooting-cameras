package com.backend.rebootingcameras.trassir_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Accessors(chain = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trassir_user_rights")
public class TrassirUserRightsInfo {

    @Id
    @Column(name = "guid")
    private String guid;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "base_rights")
    private Integer baseRights;

    @Column(name = "acl")
    private String acl;

    @Column(name = "channels")
    private String channels;

    @Column(name = "server_guid")
    private String serverGuid;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "group_id")
    private String groupId;

}
