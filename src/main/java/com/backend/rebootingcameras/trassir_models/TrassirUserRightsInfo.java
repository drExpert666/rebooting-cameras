package com.backend.rebootingcameras.trassir_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trassir_user_rights")
public class TrassirUserRightsInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guid")
    private String guid;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "base_rights")
    private String baseRights;

    @Column(name = "acl")
    private String acl;

    @Column(name = "channels")
    private String channels;

    @Column(name = "server_guid")
    private String serverGuid;

}
