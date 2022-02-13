package com.backend.rebootingcameras.utils;

import com.backend.rebootingcameras.trassir_models.TrassirUserRightsInfo;

import java.util.Comparator;

/* класс-компаратор для сравнения прав пользователя по айди группы */
public class UserRightsComparator implements Comparator<TrassirUserRightsInfo> {
    @Override
    public int compare(TrassirUserRightsInfo o1, TrassirUserRightsInfo o2) {
        if (o1.getGroupId() == null && o2.getGroupId() == null) {
            return 0;
        }
        if (o1.getGroupId() == null && o2.getGroupId() != null) {
            return 1;
        }
        if (o1.getGroupId() != null && o2.getGroupId() == null) {
            return -1;
        } else {
            return o1.getGroupId().compareTo(o2.getGroupId());
        }
    }
}
