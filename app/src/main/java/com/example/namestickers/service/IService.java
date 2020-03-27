package com.example.namestickers.service;

import java.util.List;

public interface IService {

    int retrieveTable();

    int saveTable();

    void createTable();

    int NScrolls(String sticker);

    void insertSticker(String key);

    void insertNUnnamed(int n);

    boolean remove(String sticker);

    boolean rename(String key, String newName);

    void setStickersPerRow(int n);

    int getStickersPerRow();

    int size();

    List<String> stickersInRange(int i, int j);
}
