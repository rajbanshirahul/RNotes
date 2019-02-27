package np.com.rahulrajbanshi.rnotes.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for identifying table and columns for notes table
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_TITLE = "noteTitle";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_CREATED = "noteCreated";
    public static final String NOTE_COLOUR = "noteColour";
    public static final String NOTE_FONT_SIZE = "noteFontSize";
    public static final String NOTE_BODY_HIDDEN = "noteBodyHidden";

    public static final String[] NOTES_ALL_COLUMNS = {NOTE_ID, NOTE_TITLE, NOTE_TEXT, NOTE_CREATED, NOTE_COLOUR, NOTE_FONT_SIZE, NOTE_BODY_HIDDEN};

    //SQL to create notes table
    private static final String NOTES_TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NOTE_TITLE + " TEXT, " +
                    NOTE_TEXT + " TEXT, " +
                    NOTE_CREATED + " TEXT, " +
                    NOTE_COLOUR + " INTEGER, " + NOTE_FONT_SIZE + " INTEGER, " + NOTE_BODY_HIDDEN + " INTEGER" + ")";


    //Constants for identifying table and columns for groups table
    public static final String TABLE_GROUPS = "groups";
    public static final String GROUP_ID = "_id";
    public static final String GROUP_NAME = "group_name";

    public static final String[] GROUPS_ALL_COLUMNS = {GROUP_ID, GROUP_NAME};

    //SQL to create groups table
    private static final String GROUPS_TABLE_CREATE =
            "CREATE TABLE " + TABLE_GROUPS + " (" +
                    GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GROUP_NAME + " TEXT" + ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(NOTES_TABLE_CREATE);
        sqLiteDatabase.execSQL(GROUPS_TABLE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        onCreate(sqLiteDatabase);

    }
}
