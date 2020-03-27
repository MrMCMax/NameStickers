package com.example.namestickers.service;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class Service implements IService {

    /* For persistence purposes: pathnames etc */
    private static String DIRECTORY;                  //Initialized everytime the app is launched (because it can change)
    private static final String FILENAME = "table";   //Always the same name for the sticker table

    /* Attributes of the class */
    private int stickersPerRow;
    StickerTable stickerTable;

    public Service(int stickersPerRow, String defaultDirectory) {
        this.stickersPerRow = stickersPerRow;
        this.DIRECTORY = defaultDirectory;
        stickerTable = null;
    }

    /* Constants for the return code of getTable */
    public static final int TABLE_FOUND = 0;
    public static final int TABLE_NOT_FOUND = 1;
    public static final int IO_ERROR = 2;
    public static final int CLASS_ERROR = 3;
    /**
     * Find the table or create one
     */
    public int retrieveTable() {
        //Absolute path to the sticker table
        String path = DIRECTORY + File.separator + FILENAME;
        //Retrieve it
        try {
            FileInputStream inputStream = new FileInputStream(new File(path));
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);

            stickerTable = (StickerTable) objInputStream.readObject();
            //If no exception was thrown, the object was read successfully
            objInputStream.close();
            inputStream.close();
            return TABLE_FOUND;
        }
        catch (FileNotFoundException e) {
            //Table was not found. Ask the user if he wants to create a new one.
            Log.e("FNFEx", "FNFException: " + e);
            return TABLE_NOT_FOUND;
        }
        catch (IOException e) {
            Log.e("IOEx", "IOException: " + e);
            return IO_ERROR;
        }
        catch (ClassNotFoundException e) {
            Log.e("CNFEx", "CNFException: " + e);
            return CLASS_ERROR;
        }
    }

    public static final int TABLE_SAVED = 0;
    public static final int NOT_SERIALIZABLE = 4;
    public static final int FILE_ERROR = 5;

    @Override
    public int saveTable() {
        Log.d("Service", "Saving");
        String path = DIRECTORY + File.separator + FILENAME;
        //save object
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(path));
            ObjectOutputStream objOutputStream = new ObjectOutputStream(outputStream);

            objOutputStream.writeObject(stickerTable);
            //If no exception was thrown, write was successful
            objOutputStream.close();
            outputStream.close();
            return TABLE_SAVED;
        } catch (InvalidClassException icex) {
            return CLASS_ERROR;
        } catch (NotSerializableException nsex)  {
            return NOT_SERIALIZABLE;
        } catch (FileNotFoundException fnfex) {
            return FILE_ERROR;
        } catch (IOException ioex) {
            return IO_ERROR;
        }
    }

    @Override
    public void createTable() {
        stickerTable = new StickerTable(600);
    }

    @Override
    public int getStickersPerRow() {
        return stickersPerRow;
    }

    @Override
    public int size() {
        return stickerTable.size();
    }

    @Override
    public List<String> stickersInRange(int i, int j) {
        return stickerTable.stickersInRange(i, j);
    }

    @Override
    public int NScrolls(String sticker) {
        Integer pos = stickerTable.get(sticker);
        if (pos == null) {
            return -1;
        } else {
            //the first sticker in the table is the last sticker in the collection
            pos = stickerTable.size() - 1 - pos;
            return pos / stickersPerRow;
        }
    }

    @Override
    public void insertSticker(String key) {
        stickerTable.insertSticker(key);
    }

    @Override
    public void insertNUnnamed(int n) {
        stickerTable.addNewUnnamed(n);
    }

    @Override
    public boolean remove(String sticker) {
        return stickerTable.remove(sticker);
    }

    @Override
    public boolean rename(String key, String newName) {
        return stickerTable.rename(key, newName);
    }

    @Override
    public void setStickersPerRow(int n) {
        stickersPerRow = n;
    }
}
