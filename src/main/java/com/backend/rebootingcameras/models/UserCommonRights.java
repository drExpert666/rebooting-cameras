package com.backend.rebootingcameras.models;

import java.util.HashMap;

public class UserCommonRights {

    HashMap<Integer, String> addedRights = new HashMap<>();
    HashMap<Integer, String> deletedRights = new HashMap<>();

    public HashMap<Integer, String> fillAddedRights() {
        addedRights.put(0, "Просмотр");
        addedRights.put(1, "Просмотр архива");
        addedRights.put(2, "Управление");
        addedRights.put(3, "Настройка");
        addedRights.put(4, "Не используется");
        addedRights.put(5, "Редактировать закладки архива");
        addedRights.put(6, "Настройка пользователей и скриптов");
        addedRights.put(7, "Не используется");
        addedRights.put(8, "Экспортировать архив, скриншоты");
        addedRights.put(9, "Использовать PTZ");
        addedRights.put(10, "Слушать звук");
        return addedRights;
    }

    public HashMap<Integer, String> fillDeletedRights() {
        deletedRights.put(32, "Просмотр");
        deletedRights.put(33, "Просмотр архива");
        deletedRights.put(34, "Управление");
        deletedRights.put(35, "Настройка");
        deletedRights.put(36, "Не используется");
        deletedRights.put(37, "Редактировать закладки архива");
        deletedRights.put(38, "Настройка пользователей и скриптов");
        deletedRights.put(39, "Не используется");
        deletedRights.put(40, "Экспортировать архив, скриншоты");
        deletedRights.put(41, "Использовать PTZ");
        deletedRights.put(42, "Слушать звук");
        return deletedRights;
    }

}
