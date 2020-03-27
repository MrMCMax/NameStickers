package com.example.namestickers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.namestickers.service.IService;
import com.example.namestickers.service.Service;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String EMPTY_STICKER_TEXT = "Empty Sticker";
    public static final String NOTIFICATION_CHANNEL_ID = "SearchChannel";
    public static final String NOTIFICATION_CHANNEL_NAME = "PersistentChannel";

    private IService service;

    private LinearLayout[] columns;

    private LinearLayout stickerLayout;

    private int stickersPerColumn;

    private ImageButton upArrowButton;
    private ImageButton downArrowButton;

    /** Top row of the sticker view. 0 is the newest row */
    private int currentRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String TAG = MainActivity.class.getSimpleName();
        service = new Service(4, getFilesDir().getAbsolutePath());
        setContentView(R.layout.activity_main);

        //Set listener for save button
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int code = service.saveTable();
                if (code != Service.TABLE_SAVED) {
                    outputErrorHandle(code);
                }
            }
        });
        Button addUnnamedButton = (Button) findViewById(R.id.addUnnamedButton);
        addUnnamedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUnnamed();
            }
        });
        Button addNamedButton = (Button) findViewById(R.id.addNamedButton);
        addNamedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNamed();
            }
        });

        upArrowButton = findViewById(R.id.upArrowButton);
        downArrowButton = findViewById(R.id.downArrowButton);

        upArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upStickers();
            }
        });
        downArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downStickers();
            }
        });

        //Set additional layout. Gets the table once finished
        setLayout();

        //Set notification :)
        setNotification();
    }

    /**
     * Precondition: stickersPerRow > 0
     */
    private void setLayout() {
        stickerLayout = findViewById(R.id.stickerLayout);

        stickerLayout.removeAllViews();

        columns = new LinearLayout[service.getStickersPerRow()];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new LinearLayout(this);
            columns[i].setOrientation(LinearLayout.VERTICAL);
            columns[i].setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f));
            stickerLayout.addView(columns[i]);
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i].addView(getNewStickerImageButton());
            Log.d("column " + i + ":", "added ib");
        }
        final ImageButton ib = getNewStickerImageButton();
        ib.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ib.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                tryHeight();
            }
        });
        columns[0].addView(ib);
    }

    /*
     * Gets the last sticker in the first column and tries to add a new one. If it's not possible,
     * sets stickersPerColumn and calls populateView to fill in the stickers
     */
    private void tryHeight() {
        int lastIndex = columns[0].getChildCount() - 1;
        int beforeLastIndex = lastIndex - 1;
        ImageButton aux = (ImageButton) columns[0].getChildAt(lastIndex);
        ImageButton aux2 = (ImageButton) columns[0].getChildAt(beforeLastIndex);
        Log.d("ImageButton", "ultimo: " + aux.getBottom());
        Log.d("ImageButton", "penultimo: " + aux.getBottom());
        if (aux.getBottom() > aux2.getBottom()) {
            final ImageButton next = getNewStickerImageButton();
            columns[0].addView(next);
            next.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    next.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    tryHeight();
                }
            });
        } else {
            //Remove the last one, that clips.
            columns[0].removeView(aux);
            stickersPerColumn = columns[0].getChildCount();
            populateView();
        }
    }

    /**
     * Fills the view with sticker buttons given stickersPerRow and stickersPerColumn.
     * Then calls getTable to retrieve the table and set the stickers
     */
    private void populateView() {
        for (int i = 0; i < columns.length; i++) {
            columns[i].removeAllViews();
            columns[i].setBaselineAligned(false);
            for (int j = 0; j < stickersPerColumn; j++) {
                columns[i].addView(getNewStickerButton());
            }
        }
        getTable();
    }

    /**
     * Calls the service layer to get the table and handles its errors.
     * If it succeeds, calls setStickers to update the sticker names
     */
    private void getTable() {
        //Get table
        Log.d("Conc", "getting table");
        int code = service.retrieveTable();
        Log.d("Code: ", code + "");
        if (code == Service.TABLE_FOUND) {
            setInitialStickers();
        } else {
            inputErrorHandle(code);
        }

    }

    /**
     * Populates the view with "sitckersPerColumn" stickers
     * Prone to changes
     */
    private void setInitialStickers() {
        int nStickers = columns.length * stickersPerColumn; //Local amount in view
        int lowBound = service.size() - nStickers;
        if (lowBound < 0) {
            //Not enough stickers to display
            lowBound = 0;
            downArrowButton.setEnabled(false);
        }
        List<String> items = service.stickersInRange(lowBound, service.size());
        int stickerIndex;
        String name;
        for (int i = 0; i < columns.length; i++) {
            for (int j = 0; j < stickersPerColumn; j++) {
                Button b = (Button) columns[i].getChildAt(j);
                stickerIndex = items.size() - j * columns.length - i - 1;   //Correct position of the local array
                if (stickerIndex < 0) {
                    name = EMPTY_STICKER_TEXT;
                    b.setEnabled(false);
                } else {
                    name = items.get(stickerIndex);
                    b.setEnabled(true);
                }
                b.setText(name);
                Log.d("button name", name);
                Log.d("populate", "Adding buttons");
            }
        }
        currentRow = 0;
        upArrowButton.setEnabled(false);
    }

    private List<String> getRow(int col) {
        int firstIndex = service.size() - col * columns.length - 1;
        if (firstIndex < 0) {
            return new ArrayList<String>();
        }
        int lastIndex = firstIndex - columns.length;
        if (lastIndex < 0) {
            lastIndex = 0;
        }
        return service.stickersInRange(firstIndex, lastIndex);
    }

    private void downStickers() {
        List<String> stickers = getRow(currentRow + stickersPerColumn);
        if (stickers.size() < columns.length) {
            downArrowButton.setEnabled(false);
        }
        int i, j;
        Button aux1, aux2;
        for (i = 0; i < stickersPerColumn - 1; i++) {
            for (j = 0; j < columns.length; j++) {
                aux1 = (Button) columns[j].getChildAt(i);
                aux2 = (Button) columns[j].getChildAt(i + 1);
                aux1.setText(aux2.getText());
            }
        }
        i = 0;
        while (i < columns.length) {
            aux1 = (Button) columns[i].getChildAt(stickersPerColumn - 1);
            if (i < stickers.size()) {
                aux1.setText(stickers.get(i));
                aux1.setEnabled(true);
            } else {
                aux1.setText(EMPTY_STICKER_TEXT);
                aux1.setEnabled(false);
            }
        }
        currentRow++;
        upArrowButton.setEnabled(true);
    }

    private void upStickers() {
        List<String> stickers = getRow(currentRow - 1);
        int i, j;
        Button aux1, aux2;
        for (i = 1; i < stickersPerColumn; i++) {
            for (j = 0; j < columns.length; j++) {
                aux1 = (Button) columns[j].getChildAt(i - 1);
                aux2 = (Button) columns[j].getChildAt(i);
                aux2.setText(aux1.getText());
            }
        }
        i = 0;
        while (i < columns.length) {
            aux1 = (Button) columns[i].getChildAt(0);
            if (i < stickers.size()) {
                aux1.setText(stickers.get(i));
            } else {
                aux1.setText(EMPTY_STICKER_TEXT);
            }
        }
        currentRow--;
        downArrowButton.setEnabled(true);
        if (currentRow == 0) {
            upArrowButton.setEnabled(false);
        }
    }

    private Button getNewStickerButton() {
        Button b = new Button(this);
        b.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        return b;
    }

    private ImageButton getNewStickerImageButton() {
        ImageButton ib = new ImageButton(this);
        ib.setImageResource(R.mipmap.ic_launcher);

        ib.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return ib;
    }

    private void setNotification() {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager nm  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle("Find sticker...");
    }


    private void inputErrorHandle(int code) {
        switch (code) {
            case Service.TABLE_NOT_FOUND:
                //Show an alert to let the user create a new table
                new AlertDialog.Builder(this)
                    .setTitle("Table not found")
                    .setMessage("No previous sticker table on this app was found. Do you want to create a new one?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            service.createTable();
                            setInitialStickers();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Let the user find a sticker table located somewhere
                            findTableFile(dialog, which);
                            setInitialStickers();
                        }
                    })
                    .show();
                break;

            case Service.CLASS_ERROR:
                new AlertDialog.Builder(this)
                    .setTitle("File outdated")
                    .setMessage("The sticker table belongs to an outdated version of the app")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            findTableFile(dialog, which);
                        }
                    })
                    .show();
                break;

            default: //IOError
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("An unexpected error has occurred. The application will close")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                System.exit(2);
                            }
                        })
                        .show();
                break;
        }
    }

    public void findTableFile(DialogInterface dialog, int which) {
        new AlertDialog.Builder(this)
            .setTitle("Find table")
            .setMessage("Do you want to search for the sticker table in the file system?")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AlertDialog.Builder(MainActivity.this).setMessage("Not supported yet").show();
                    finish();
                    System.exit(1);

                }
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Let the user find a sticker table located somewhere
                    finish();
                    System.exit(0);
                }
            })
            .show();
    }

    private void outputErrorHandle(int code) {
        Log.d("service error", code + "");
    }

    private void addUnnamed() {

    }

    private void addNamed() {

    }
}
