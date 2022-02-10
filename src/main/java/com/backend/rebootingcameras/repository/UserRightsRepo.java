package com.backend.rebootingcameras.repository;

import com.backend.rebootingcameras.trassir_models.TrassirUserRightsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRightsRepo extends JpaRepository<TrassirUserRightsInfo, String> {

    @Query("select u from TrassirUserRightsInfo u where u.channels like concat('%',:channelsGuid,'%')")
    List<TrassirUserRightsInfo> findUsersByChannel(@Param("channelsGuid") String channelsGuid);

    List<TrassirUserRightsInfo> findAllByGroupId(String groupId);

    TrassirUserRightsInfo findByGuid(String guid);
}
