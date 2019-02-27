package np.com.rahulrajbanshi.rnotes.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import np.com.rahulrajbanshi.rnotes.R;
import np.com.rahulrajbanshi.rnotes.adapter.MyCursorAdapter;
import np.com.rahulrajbanshi.rnotes.helper.DBOpenHelper;
import np.com.rahulrajbanshi.rnotes.model.NoteModel;
import np.com.rahulrajbanshi.rnotes.provider.NotesProvider;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EDITOR_REQUEST_CODE = 1001;
    public static final String BACKUP_FOLDER_PATH = "/RNotes"; // Backup folder path
    final String BACKUP_FILE_NAME = "rnotes_backup.json"; // Backup file name

    private MyCursorAdapter customCursorAdapter;

    Toolbar toolbar;
    FloatingActionButton fab;

    Map<Integer, String> noteFilterMap = new HashMap<Integer, String>();
    private int countSelection = 0;

    Cursor cursorData;

    private int lastFirstVisibleItem = -1; // Last first item seen in list view scroll changed
    private float newNoteButtonBaseYCoordinate; // Base Y coordinate of newNote button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Android version >= 18 -> set orientation userPortrait
        if (Build.VERSION.SDK_INT >= 18)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
            // Android version < 18 -> set orientation sensorPortrait
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setContentView(R.layout.activity_main);

        setUpToolbar();

        implementMaterialDesign();

        implementFloatingActionButton();

        setUpListView();

        getLoaderManager().initLoader(0, null, this);

    }

    private void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setUpListView() {

        customCursorAdapter = new MyCursorAdapter(this, null, 0);

        final ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(customCursorAdapter);

        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // If last first visible item not initialized -> set to current first
                if (lastFirstVisibleItem == -1)
                    lastFirstVisibleItem = view.getFirstVisiblePosition();

                // If scrolled up -> hide newNote button
                if (view.getFirstVisiblePosition() > lastFirstVisibleItem) {
                    newNoteButtonVisibility(false);
                }

                // If scrolled down and delete/search not active -> show newNote button
                else if (view.getFirstVisiblePosition() < lastFirstVisibleItem) {
                    newNoteButtonVisibility(true);
                }

                // Set last first visible item to current
                lastFirstVisibleItem = view.getFirstVisiblePosition();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }

        });
        list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {

                if (b) {
                    String s = DBOpenHelper.NOTE_ID + "=" + l;
                    noteFilterMap.put(Integer.valueOf(i), s);
                    countSelection++;
                    actionMode.setTitle(countSelection + " items selected");

                    //new code
                    customCursorAdapter.setNewSelection(i, b);
                } else {
                    noteFilterMap.remove(Integer.valueOf(i));
                    countSelection--;
                    actionMode.setTitle(countSelection + " items selected");

                    //new code
                    customCursorAdapter.removeSelection(i);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                getMenuInflater().inflate(R.menu.ctxtl_menu_main, menu);
                fab.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {

                final List<String> list = new ArrayList<String>(noteFilterMap.values());
                int ctxtlMenuItemId = menuItem.getItemId();

                switch (ctxtlMenuItemId) {
                    case R.id.action_delete_ctxtl: {
                        DialogInterface.OnClickListener dialogClickListener =
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int button) {
                                        if (button == DialogInterface.BUTTON_POSITIVE) {
                                            //Insert Data management code here

                                            for (int i = 0; i < list.size(); i++) {
                                                getContentResolver().delete(NotesProvider.CONTENT_URI, list.get(i), null);
                                            }
                                            onDestroyActionMode(actionMode);
                                            Toast.makeText(MainActivity.this,
                                                    getString(R.string.delete_successful),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage(getString(R.string.delete_selected_notes))
                                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                                .show();
                    }
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

                countSelection = 0;
                noteFilterMap.clear();

                //new code
                customCursorAdapter.clearSelection();
                fab.setVisibility(View.VISIBLE);
                restartLoader();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + l);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });
    }

    /* method for implementing FloatingPointAction button.  */
    private void implementFloatingActionButton() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newNote();
            }
        });
    }

    /**
     * Method to show and hide the newNote button
     *
     * @param isVisible true to show button, false to hide
     */
    protected void newNoteButtonVisibility(boolean isVisible) {
        if (isVisible) {
            fab.animate().cancel();
            fab.animate().translationY(newNoteButtonBaseYCoordinate);
        } else {
            fab.animate().cancel();
            fab.animate().translationY(newNoteButtonBaseYCoordinate + 500);
        }
    }

    /* Launches EditorActivity for new note */
    private void newNote() {
        Intent intent = new Intent(MainActivity.this, EditorActivity.class);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    private void implementMaterialDesign() {

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            findViewById(R.id.shadow_toolbar_main).setVisibility(View.GONE);
        }
    }

    private void insertNote(String noteTitle, String noteText) {

        /*Creating Date and Time*/
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle);
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        /*Formatting the Date and Time and storing in content values.*/
        values.put(DBOpenHelper.NOTE_CREATED, dateFormat.format(date));

        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_new_note:
                newNote();
                break;
            case R.id.action_backup_notes:
                /* First check permission for ExternalStorageWritePermission
                 * and then call backup operation code*/
                checkExternalStorageWritePermissionAndAskForPermission();
                break;
            case R.id.action_restore_notes:
                /* First check permission for ExternalStorageReadPermission
                 * and then call restore operation code*/
                checkExternalStorageReadPermissionAndAskForPermission();
                break;
            case R.id.action_delete_all:
                deleteAllNotes();
                break;
            case R.id.action_create_sample:
                insertSampleNotes();
                break;
            case R.id.action_about:
                Intent i = new Intent(this, AboutActivity.class);
                startActivity(i);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* This method is called after ExternalStorageReadPermission is checked
    and permission is granted. */
    private void restoreNotes() {
        // 'Restore notes?' confirmation dialog.
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //restore operation code here
                            readFromExternalPublicStorage();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.restore_cancelled),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.restore_notes))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    /* This method is called after ExternalStorageWritePermission is checked and permission is granted
     * and shows the 'backup notes?' confirmation dialog. */
    private void backupNotes() {
        // 'backup notes?' confirmation dialog.
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //backup operation code here
                            String jsonString = cursorToJSONStringOperation();
                            if (jsonString != null) {
                                saveToExternalPublicStorage(jsonString);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.notes_not_found_in_the_database,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.backup_notes))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    /* This method converts the cursor object to JSON string. */
    private String cursorToJSONStringOperation() {
        ArrayList<NoteModel> notesArray = new ArrayList<NoteModel>();

        if (cursorData.moveToFirst()) {
            do {
                NoteModel noteModel = new NoteModel();
                noteModel.setNoteTitle(cursorData.getString(cursorData.getColumnIndex(DBOpenHelper.NOTE_TITLE)));
                noteModel.setNoteText(cursorData.getString(cursorData.getColumnIndex(DBOpenHelper.NOTE_TEXT)));
                noteModel.setNoteCreated(cursorData.getString(cursorData.getColumnIndex(DBOpenHelper.NOTE_CREATED)));
                noteModel.setColour(cursorData.getInt(cursorData.getColumnIndex(DBOpenHelper.NOTE_COLOUR)));
                noteModel.setFontSize(cursorData.getInt(cursorData.getColumnIndex(DBOpenHelper.NOTE_FONT_SIZE)));
                noteModel.setBodyHidden(cursorData.getInt(cursorData.getColumnIndex(DBOpenHelper.NOTE_BODY_HIDDEN)));
                notesArray.add(noteModel);
            } while (cursorData.moveToNext());
            Gson gson = new Gson();
            return gson.toJson(notesArray);
        }
        return null;
    }

    // json String to ArrayList of NoteModel objects conversion
    private ArrayList<NoteModel> convertStringToArrayListOfNoteModel(String fileData) {

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<NoteModel>>() {
        }.getType();
        ArrayList<NoteModel> notesArray = null;
        try {
            notesArray = gson.fromJson(fileData, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notesArray;
    }

    /* This method is called after the 'restore notes?' confirmation dialog
    returns positive result or 'Ok' button is clicked. */
    private void readFromExternalPublicStorage() {
        // check whether backup file exists or not and storage is readable or not.

        File backupFolder = new File(Environment.getExternalStorageDirectory() +
                BACKUP_FOLDER_PATH);
        File backupFileWithPath = new File(backupFolder, BACKUP_FILE_NAME);

        if (isExternalStorageReadable() && backupFileWithPath.exists()) {
            // file read operation
            String fileData = readFromFile(backupFileWithPath);
            ArrayList<NoteModel> noteModelArrayList = convertStringToArrayListOfNoteModel(fileData);

            if (noteModelArrayList != null) {
                // if not null, call method for data insertion to database
                restoreDataToDatabase(noteModelArrayList);
            } else {
                Toast.makeText(this, getString(R.string.invalid_backup_file), Toast.LENGTH_SHORT).show();
            }

        } else if (isExternalStorageReadable() && !backupFileWithPath.exists()) {
            Toast.makeText(this, getString(R.string.backup_file_not_found), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.permission_denied)
                    + "\n" + getString(R.string.storage_not_readable), Toast.LENGTH_SHORT).show();
        }
    }

    /* This method is called after successful retrieval of data from database to JSON string. */
    private void saveToExternalPublicStorage(String jsonStringData) {

        if (isExternalStorageWritable()) {
            File backupDir = new File(Environment.getExternalStorageDirectory() + BACKUP_FOLDER_PATH);
            if (isExternalStorageReadable() && isExternalStorageWritable() && !backupDir.exists()) {
                backupDir.mkdir();
            }

            File file = new File(backupDir, BACKUP_FILE_NAME);

            checkOldBackupFileExistence(file, jsonStringData);

        } else {
            Toast.makeText(this, getString(R.string.external_media_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    /* Check whether 'Old' BackupFile exist or not. If does not exists, perform write to file operation
     * and if exists show dialog for 'overwrite existing backup file?', If user clicks 'Ok' button,
     * then perform write to file operation.*/
    private void checkOldBackupFileExistence(final File file, final String data) {
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            if (!file.exists()) {
                writeToFile(file, data);
            } else {
                //overwrite existing file? code here
                DialogInterface.OnClickListener dialogClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                                if (button == DialogInterface.BUTTON_POSITIVE) {
                                    writeToFile(file, data);
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.backup_cancelled), Toast.LENGTH_SHORT).show();
                                }
                            }
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.overwrite_existing_backup_file))
                        .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                        .show();
            }
        } else {
            // If external storage not readable/writable toast 'Permission Denied'.
            Toast.makeText(getApplicationContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
        }
    }

    /* read data from backup file after 'file existence' and 'storage readable' check. */
    private String readFromFile(File file) {

        BufferedReader bufferedReader;
        StringBuilder text = new StringBuilder();
        try {
            // Initialize BufferedReader, read from 'fromFile' and store into root object
            bufferedReader = new BufferedReader(new FileReader(file));
            text = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
            }

            // close the BufferedReader
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    /* Write data to backup file after oldBackupFileExistence check. */
    private void writeToFile(File file, String data) {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            // show backup successful toast
            Toast.makeText(this, getString(R.string.notes_backup_successful) + "!\n"
                            + getString(R.string.dialog_backup_created) + " " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

            // close the FileOutputStream
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // show backup failed toast
            Toast.makeText(this, getString(R.string.notes_backup_error),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            // show backup failed toast
            Toast.makeText(this, getString(R.string.notes_backup_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check if external storage is writable or not
     *
     * @return true if writable, false otherwise
     */
    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Check if external storage is readable or not
     *
     * @return true if readable, false otherwise
     */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /* Check external storage write permission state and ask for permission. */
    private void checkExternalStorageWritePermissionAndAskForPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 30);
        } else {
            // code for already granted permission
            backupNotes();
        }
    }

    /* Check external storage read permission and ask for permission */
    private void checkExternalStorageReadPermissionAndAskForPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 25);
        } else {
            // code for already granted permission
            restoreNotes();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case 25: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // call for restoreNotes() if permission is granted
                    restoreNotes();
                } else { // Permission Denied : Disable the functionality related to the permission
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
            break;

            case 30: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // call backupNotes() if permission is granted
                    backupNotes();
                } else {
                    // Permission Denied : Disable the functionality related to the permission
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /* Delete all notes from the database. */
    private void deleteAllNotes() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getContentResolver().delete(NotesProvider.CONTENT_URI, null, null);
                            restartLoader();
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_all_notes))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    /* Insert sample notes to the database.*/
    private void insertSampleNotes() {
        insertNote(getString(R.string.sample_note1_title), getString(R.string.sample_note1_body));
        insertNote(getString(R.string.sample_note2_title), getString(R.string.sample_note2_body));
        insertNote(getString(R.string.sample_note3_title), getString(R.string.sample_note3_body));
        insertNote(getString(R.string.sample_note4_title), getString(R.string.sample_note4_body));
        insertNote(getString(R.string.sample_note5_title), getString(R.string.sample_note5_body));
        insertNote(getString(R.string.sample_note6_title), getString(R.string.sample_note6_body));
        restartLoader();
    }

    /* This method restores or inserts multiple row data to database from
    arrayList of NoteModel using 'bulkInsert()' method. */
    private void restoreDataToDatabase(ArrayList<NoteModel> noteModelArrayList) {

        /* get arrayList of ContentValues, then insert it into the database,
        then toast 'backup successful' and then restart the Loader. */
        ContentValues[] contentValuesArray = getArrayOfContentValues(noteModelArrayList);
        getContentResolver().bulkInsert(NotesProvider.CONTENT_URI, contentValuesArray);

        // display 'restore successful' toast message
        Toast.makeText(this, getString(R.string.restore_notes_successful), Toast.LENGTH_LONG).show();
        restartLoader();
    }

    /* This method returns array of 'ContentValues object' from arrayList
    of NoteModel objects. */
    private ContentValues[] getArrayOfContentValues(ArrayList<NoteModel> noteModelArrayList) {

        ContentValues[] contentValuesArray = new ContentValues[noteModelArrayList.size()];

        for (int i = 0; i < noteModelArrayList.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(DBOpenHelper.NOTE_TITLE, noteModelArrayList.get(i).getNoteTitle());
            values.put(DBOpenHelper.NOTE_TEXT, noteModelArrayList.get(i).getNoteText());
            values.put(DBOpenHelper.NOTE_CREATED, noteModelArrayList.get(i).getNoteCreated());
            values.put(DBOpenHelper.NOTE_COLOUR, noteModelArrayList.get(i).getColour());
            values.put(DBOpenHelper.NOTE_FONT_SIZE, noteModelArrayList.get(i).getFontSize());
            values.put(DBOpenHelper.NOTE_BODY_HIDDEN, noteModelArrayList.get(i).getBodyHidden());
            contentValuesArray[i] = values;
        }
        return contentValuesArray;
    }

    /* This method restarts the Loader to reload the data into list view
    after the changes performed into the database.*/
    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    /* This method is called every time before the call of onLoadFinished() method */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotesProvider.CONTENT_URI, null, null, null, null);
    }

    /* This method is called every time after the call of onCreateLoader() method */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        customCursorAdapter.swapCursor(data);

        //for backing up data to json file
        cursorData = data;
    }

    /* This method is called when Activity exits or
    Cursor object is no longer required or used. */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        customCursorAdapter.swapCursor(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
