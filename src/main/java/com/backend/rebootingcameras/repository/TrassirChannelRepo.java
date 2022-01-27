package com.backend.rebootingcameras.repository;

import com.backend.rebootingcameras.entity.Switch;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrassirChannelRepo extends JpaRepository<TrassirChannelInfo, String> {

    TrassirChannelInfo findByGuidChannel(String guid);

    @Query("select s from TrassirChannelInfo s " +
            "where (:guidServer is null or :guidServer = '' or lower(s.guidServer) like lower(concat('%',:guidServer,'%') )) " +
            "and (:guidChannel is null or :guidChannel = '' or lower(s.guidChannel) like lower(concat('%',:guidChannel,'%'))) " +
            "and (:name is null or :name = '' or lower(s.name) like lower(concat('%',:name,'%'))) " +
            "and (:signal is null or s.signal = :signal)" +
            "and (:switchId is null or s.switchId.id = :switchId)")
    Page<TrassirChannelInfo> findByParams(@Param("guidServer")String guidServer,
                                          @Param("guidChannel")String guidChannel,
                                          @Param("name") String name,
                                          @Param("signal") Integer signal,
                                          @Param("switchId") Long switchId,
                                          Pageable pageable);

}
