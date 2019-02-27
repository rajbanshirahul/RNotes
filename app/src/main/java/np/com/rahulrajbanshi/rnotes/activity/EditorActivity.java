package np.com.rahulrajbanshi.rnotes.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.thebluealliance.spectrum.SpectrumDialog;

import java.text.SimpleDateFormat;
import java.util.Date;

import np.com.rahulrajbanshi.rnotes.R;
import np.com.rahulrajbanshi.rnotes.constants.Constants;
import np.com.rahulrajbanshi.rnotes.helper.DBOpenHelper;
import np.com.rahulrajbanshi.rnotes.provider.NotesProvider;

public class EditorActivity extends AppCompatActivity {

    private EditText editor, noteTitleEt;
    private String oldNoteTitle, oldText;
    private int noteColourNewState, noteColourOldState;
    private int noteFontNewSize, noteFontOldSize;
    private int[] fontSizeArr; // Font sizes int array
    private int noteBodyHiddenNewState, noteBodyHiddenOldState;
    private String action;
    private String noteFilter;
    private LinearLayout parentLinearLayoutActivityEditor;
    private Menu menuEditorActivity;

    CardView cardViewEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        /** NOTE: Do not alter the calling sequence of the methods
         * inside this onCreate() method otherwise problem may occur */

        // Set up toolbar and actionbar
        setUpToolbar();

        // initialize views
        initializeViews();

        fontSizeArr = new int[]{14, 18, 22}; // Note font size 0 for small, 1 for medium, 2 for large

        // Set up activity variables and activity state.
        setupActivityVariablesAndState();
    }

    /* Set up toolbar and actionbar. This method is called from the onCreate() method.*/
    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /* Initialize Views. This method is called from the onCreate() method. */
    private void initializeViews() {
        editor = (EditText) findViewById(R.id.editText);
        noteTitleEt = (EditText) findViewById(R.id.titleEdit);
        cardViewEditor = (CardView) findViewById(R.id.cardViewEditorActivity);
        parentLinearLayoutActivityEditor = (LinearLayout) findViewById(R.id.parentLinearLayoutActivityEditor);
    }

    /* Set up activity variables and activity state. This method is called from
    the onCreate() method.*/
    private void setupActivityVariablesAndState() {
        Intent intent = getIntent();
        /* Below line of code is for the fetching of parcelable uri that is put on the intent object
         * by by the code "intent.putExtra()" in MainActivity.java file*/
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        if (uri == null) {
            action = Intent.ACTION_INSERT;
            noteColourNewState = cardViewEditor.getCardBackgroundColor().getDefaultColor();
            noteFontNewSize = fontSizeArr[1]; // initializing default font size (18) for new note mode.
            noteBodyHiddenNewState = 0;
            setTitle(R.string.action_new_note);
            /* the false value is passed to the setter setFocusableInTouchMode() to get the focus in new note mode (think reverse).
             * In XML it is set to true to prevent from focus or appearing a keyboard in text view or text edit mode */
            parentLinearLayoutActivityEditor.setFocusableInTouchMode(false);

        } else {
            action = Intent.ACTION_EDIT;
            setTitle(R.string.edit_note);
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.NOTES_ALL_COLUMNS, noteFilter, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            if (cursor != null) {
                oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
                oldNoteTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE));
                if (oldNoteTitle == null) {
                    oldNoteTitle = "";
                }
                /* retrieve colour from database, if the column is empty or returns 0 or throws an exception
                 then get default background colour from 'cardview' and set the appropriate values */
                try {
                    noteColourOldState = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_COLOUR));
                    if (noteColourOldState != 0) {
                        cardViewEditor.setCardBackgroundColor(noteColourOldState);
                    } else {
                        noteColourOldState = cardViewEditor.getCardBackgroundColor().getDefaultColor();
                    }
                    noteColourNewState = noteColourOldState;

                    noteFontOldSize = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_FONT_SIZE));
                    switch (noteFontOldSize) {
                        case 14:
                            noteFontNewSize = noteFontOldSize;
                            break;
                        case 18:
                            noteFontNewSize = noteFontOldSize;
                            break;
                        case 22:
                            noteFontNewSize = noteFontOldSize;
                            break;
                        default:
                            /* To make fontSize default (medium) and also to avoid 'save changes' dialog from appearing
                            when there is 0 or no value for fontSize in the database. */
                            noteFontNewSize = fontSizeArr[1];
                            noteFontOldSize = fontSizeArr[1];
                    }
                    noteBodyHiddenOldState = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_BODY_HIDDEN));
                    noteBodyHiddenNewState = noteBodyHiddenOldState;

                } catch (Exception e) {
                    /* Here even if only one exception occurs all values are set to default. So this needs to be handled. */
                    noteColourOldState = cardViewEditor.getCardBackgroundColor().getDefaultColor();
                    noteColourNewState = noteColourOldState;
                    noteFontNewSize = fontSizeArr[1];

                    noteBodyHiddenNewState = noteBodyHiddenOldState = 0;
                }
            }
            editor.setText(oldText);
            noteTitleEt.setText(oldNoteTitle);
            noteTitleEt.setFocusableInTouchMode(true);
            editor.setFocusableInTouchMode(true);
            editor.setTextSize(noteFontNewSize);
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        menuEditorActivity = menu;
        if (action.equals(Intent.ACTION_INSERT)) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        if (action.equals(Intent.ACTION_EDIT)) {
            if (noteBodyHiddenOldState != 0) {
                menu.getItem(3).setTitle(R.string.action_show_note_body_in_list);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                returnToHome();
                break;

            case R.id.action_note_colour:
                chooseNoteColourDialog();
                break;

            case R.id.action_delete:
                DialogInterface.OnClickListener dialogClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                                if (button == DialogInterface.BUTTON_POSITIVE) {
                                    //Insert Data management code here
                                    deleteNote();
                                } else {

                                }
                            }
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.delete_note))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener)
                        .show();
                break;

            case R.id.action_font_size:
                fontSizeOptionDialog().show();
                break;

            case R.id.action_hide_show_body:
                showHideNoteBodyAction();
        }
        return true;
    }

    /* Show or Hide note body menu action act like 'Inverter' */
    private void showHideNoteBodyAction() {
        MenuItem menuItem = menuEditorActivity.getItem(3);
        if (noteBodyHiddenNewState == 0) {
            noteBodyHiddenNewState = 1;
            menuItem.setTitle(R.string.action_show_note_body_in_list);
        } else {
            noteBodyHiddenNewState = 0;
            menuItem.setTitle(R.string.action_hide_note_body_in_list);
        }
    }

    /* Note colour chooser dialog */
    private void chooseNoteColourDialog() {
        new SpectrumDialog.Builder(getApplicationContext()).setColors(R.array.colors_palette_array)
                .setSelectedColor(noteColourNewState)
                .setDismissOnColorSelected(true)
                .setOutlineWidth(2)
                .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                        if (positiveResult) {
                            cardViewEditor.setCardBackgroundColor(color);
                            noteColourNewState = color;
                        } else {
                        }
                    }
                }).build().show(getSupportFragmentManager(), "note_color_chooser_dialog");
    }

    /* Select note font size dialog */
    private AlertDialog fontSizeOptionDialog() {
        final String[] options = {"Small", "Medium", "Large"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, options);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Note font size");
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        noteFontNewSize = fontSizeArr[i];
                        editor.setTextSize(fontSizeArr[i]);
                        break;
                    case 1:
                        noteFontNewSize = fontSizeArr[i];
                        editor.setTextSize(fontSizeArr[i]);
                        break;
                    case 2:
                        noteFontNewSize = fontSizeArr[i];
                        editor.setTextSize(fontSizeArr[i]);
                        break;
                    default:
                        noteFontNewSize = fontSizeArr[1];
                        editor.setTextSize(fontSizeArr[1]);
                }
            }
        });
        return builder.create();
    }

    /* This method is executed when back navigation button is clicked or
     * back button on the phone is pressed */
    private void returnToHome() {
        String newNoteTitle = noteTitleEt.getText().toString().trim();
        String newNoteText = editor.getText().toString().trim();

        if (action.equals(Intent.ACTION_EDIT)) {
            /* If nothing is changed and back button is clicked, then go to the home
             * otherwise prompt the 'save changes?' dialog. */
            if (oldText.equals(newNoteText) && oldNoteTitle.equals(newNoteTitle)
                    && noteColourOldState == noteColourNewState && noteFontOldSize == noteFontNewSize
                    && noteBodyHiddenOldState == noteBodyHiddenNewState) {
                setResult(RESULT_CANCELED);
                finish();
            } else if (!oldText.equals(newNoteText) || !oldNoteTitle.equals(newNoteTitle)
                    || noteColourOldState != noteColourNewState || noteFontOldSize != noteFontNewSize
                    || noteBodyHiddenOldState != noteBodyHiddenNewState) {
                DialogInterface.OnClickListener dialogClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                                if (button == DialogInterface.BUTTON_POSITIVE) {
                                    //Insert Data management code here or call finishEditing()
                                    finishEditing();
                                } else if (button == DialogInterface.BUTTON_NEGATIVE) {
                                    setResult(RESULT_CANCELED);
                                    finish();
                                } else {

                                }
                            }
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.save_changes))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener)
                        .setNeutralButton(getString(R.string.cancel), dialogClickListener)
                        .show();
            }
        } else {
            finishEditing();
        }
    }

    /* This method is called after all variables are validated
    and is called for new note insertion and update the note. */
    private void finishEditing() {

        String newNoteText = editor.getText().toString().trim();
        String newNoteTitle = noteTitleEt.getText().toString().trim();
        switch (action) {
            case Intent.ACTION_INSERT:
                //length == 0 or empty
                if (newNoteText.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote(newNoteTitle, newNoteText, noteColourNewState, noteFontNewSize, noteBodyHiddenNewState);
                }
                break;
            case Intent.ACTION_EDIT:
                if (newNoteText.length() == 0) {
                    deleteNote();
                } else {
                    updateNote(newNoteTitle, newNoteText, noteColourNewState, noteFontNewSize, noteBodyHiddenNewState);
                }
        }
        finish();

    }

    // This method deletes a single note from EditorActivity.
    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI, noteFilter, null);
        Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    // Method to insert edited or updated note.
    private void updateNote(String noteTitle, String noteText, int noteColourNewState,
                            int noteFontNewSize, int noteBodyHiddenNewState) {

        ContentValues values = prepareContentValues(noteTitle, noteText, noteColourNewState,
                noteFontNewSize, noteBodyHiddenNewState);

        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    // Method to insert new note.
    private void insertNote(String noteTitle, String noteText, int noteColourNewState,
                            int noteFontNewSize, int noteBodyHiddenNewState) {

        ContentValues values = prepareContentValues(noteTitle, noteText, noteColourNewState,
                noteFontNewSize, noteBodyHiddenNewState);

        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        Toast.makeText(this, getString(R.string.note_created), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    /* Returns object of type ContentValues from passed parameters. */
    private ContentValues prepareContentValues(String noteTitle, String noteText, int noteColourNewState,
                                               int noteFontNewSize, int noteBodyHiddenNewState) {

        /*Creating Date and Time*/
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle);
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        /*Formatting the Date and Time and storing in content values.*/
        values.put(DBOpenHelper.NOTE_CREATED, dateFormat.format(date));
        values.put(DBOpenHelper.NOTE_COLOUR, noteColourNewState);
        values.put(DBOpenHelper.NOTE_FONT_SIZE, noteFontNewSize);
        values.put(DBOpenHelper.NOTE_BODY_HIDDEN, noteBodyHiddenNewState);
        return values;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.NOTE_NEW_COLOUR, noteColourNewState);
        outState.putInt(Constants.NOTE_OLD_COLOUR, noteColourOldState);
        outState.putInt(Constants.NOTE_FONT_NEW_SIZE, noteFontNewSize);
        outState.putInt(Constants.NOTE_FONT_OLD_SIZE, noteFontOldSize);
        outState.putInt(Constants.NOTE_BODY_HIDDEN_NEW_STATE, noteBodyHiddenNewState);
        outState.putInt(Constants.NOTE_BODY_HIDDEN_OLD_STATE, noteBodyHiddenOldState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {

            noteColourNewState = savedInstanceState.getInt(Constants.NOTE_NEW_COLOUR);
            noteColourOldState = savedInstanceState.getInt(Constants.NOTE_OLD_COLOUR);

            noteFontNewSize = savedInstanceState.getInt(Constants.NOTE_FONT_NEW_SIZE);
            noteFontOldSize = savedInstanceState.getInt(Constants.NOTE_FONT_OLD_SIZE);

            noteBodyHiddenNewState = savedInstanceState.getInt(Constants.NOTE_BODY_HIDDEN_NEW_STATE);
            noteBodyHiddenOldState = savedInstanceState.getInt(Constants.NOTE_BODY_HIDDEN_OLD_STATE);

            cardViewEditor.setCardBackgroundColor(noteColourNewState);
            editor.setTextSize(noteFontNewSize);
        }
    }

    @Override
    public void onBackPressed() {
        returnToHome();
    }
}
