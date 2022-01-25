package com.backend.rebootingcameras.repository;

import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrassirServerRepo extends JpaRepository<TrassirServerInfo, String> {

    TrassirServerInfo findByGuid(String guid);


    @Query("select t from TrassirServerInfo t where " +
            "(:serverName is null or :serverName = '' or lower(t.serverName) " +
            "like lower(concat('%',:serverName,'%') ))")
    List<TrassirServerInfo> findByParams(@Param("serverName")String serverName);

}
