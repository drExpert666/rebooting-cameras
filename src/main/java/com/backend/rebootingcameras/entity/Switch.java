package com.backend.rebootingcameras.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "switch_info")
public class Switch {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "ip")
    private String ip;

    @Basic
    @Column(name = "ports")
    private String ports;

    @Basic
    @Column(name = "numbers_of_ports")
    private Integer numbersOfPorts;

    @Basic
    @Column(name = "description")
    private String description;

}
