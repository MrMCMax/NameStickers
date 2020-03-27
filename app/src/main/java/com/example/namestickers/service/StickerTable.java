package com.example.namestickers.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class StickerTable implements Serializable {

    private static final long serialVersionUID = 2L;

    private List<String> stickersInOrder;

    private Hashtable<String, Integer> stickerTable;

    public StickerTable(int n) {
        stickersInOrder = new ArrayList<String>(n);
        stickerTable = new Hashtable<>(n);
    }

    /**
     * Inserts a new named sticker.
     * O(1)
     * @param key name of the sticker
     */
    public void insertSticker(String key) {
        stickerTable.put(key, stickerTable.size()); //The value is the position in the list: the last position
        stickersInOrder.add(key);                   //Append sticker to the end of the list
    }

    /**
     * Adds "n" unnamed stickers.
     * Their names will become the position that they hold in the collection.
     * O(1)
     * @param n number of stickers to add
     */
    public void addNewUnnamed(int n) {
        for (int i = 0; i < n; i++) {
            insertSticker(stickerTable.size() + "");
        }
    }

    /**
     * Renames a sticker.
     * O(1)
     * @param key the sticker to rename
     * @param newName the new name
     * @return true iff the sticker named by key was found
     */
    public boolean rename(String key, String newName) {
        Integer res = stickerTable.remove(key);
        if (res == null) return false;
        else {
            stickerTable.put(newName, res);
            stickersInOrder.set(res, newName);
            return true;
        }
    }

    /**
     * Removes a given sticker and updates the position of the rest.
     * O(size())
     * @param sticker the name of the sticker to remove
     * @return true iff the deletion was successful
     */
    public boolean remove(String sticker) {
        Integer i = stickerTable.remove(sticker);
        if (i == null) return false;
        else {
            //Deletion has occurred. Now we have to decrease values greater than the one removed.
            stickersInOrder.remove((int) i);
            for (int j = i; j < stickersInOrder.size(); j++) {
                stickerTable.put(stickersInOrder.get(j), j); //Update sticker table indices
            }

            /*
            Enumeration<String> e = super.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                Integer j;
                if ((j = super.get(key)) > i) {
                    super.put(key, j - 1);
                }
            }
            */
            return true;
        }
    }

    /**
     * Returns how many stickers are there.
     * O(1)
     * @return the size of the sticker table
     */
    public int size() {
        return stickerTable.size();
    }

    /**
     * Gets the index of a sticker by its name.
     * O(1) thanks to the hash table
     * @param key the name of the sticker.
     * @return the index in the list
     */
    public int get(String key) {
        return stickerTable.get(key);
    }

    /**
     * Gets the name of the sticker at a specified position.
     * O(1) thanks to an array
     * @param i the position
     * @return the sticker
     */
    public String stickerAt(int i) {
        return stickersInOrder.get(i);
    }

    /**
     * Returns a list with the stickers between positions i and j
     * @param i start pos
     * @param j end pos
     * @return list with the stickers in those positions
     */
    public List<String> stickersInRange(int i, int j) {
        return stickersInOrder.subList(i, j);
    }
}
