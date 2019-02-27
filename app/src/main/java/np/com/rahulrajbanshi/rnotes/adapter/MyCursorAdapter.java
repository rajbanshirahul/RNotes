package np.com.rahulrajbanshi.rnotes.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import np.com.rahulrajbanshi.rnotes.R;
import np.com.rahulrajbanshi.rnotes.helper.DBOpenHelper;


public class MyCursorAdapter extends CursorAdapter {

    //new code
    private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();

    private LayoutInflater cursorInflater;
    private int imgThumb1 = R.drawable.note;

    private int lastPosition = -1;

    //Default Constructor
    public MyCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    //new code
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //let the adapter handle setting up the row views
        View v = super.getView(position, convertView, parent);
        CardView cv = (CardView)v;

        if (mSelection.get(position) != null) {
            // this is a selected position and set selection color
            cv.setCardBackgroundColor(Color.parseColor("#D32F2F"));
        }

        return cv;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return cursorInflater.inflate(R.layout.list_item_cardview, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Here cardViewColour in initialized to -1, to get the default colour as white colour
        int cardViewColour = -1;
        int noteFontSize = 18; // default value = 18 (medium font size).
        int noteBodyHidden = 0;
        CardView cardView = (CardView) view;

        ImageView imgThumbnail = cardView.findViewById(R.id.imgThumbnail);
        TextView noteTitle = cardView.findViewById(R.id.titleView);
        TextView noteText = cardView.findViewById(R.id.noteText);
        TextView textDate = cardView.findViewById(R.id.textDate);


        String noteTextTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE));
        String noteTextTemp = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));

        // retrieving note colour, fontSize and noteBodyHiddenState from the database
        try {
            cardViewColour = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_COLOUR));
            noteFontSize = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_FONT_SIZE));
            noteBodyHidden = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_BODY_HIDDEN));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Taking substring if the substring has more than 350 characters.
        if (noteTextTemp.length() > 350){
            noteTextTemp = noteTextTemp.substring(0, 350);
        }

        String textDateTemp = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CREATED));

        /*Parsing Date and Time from Database and converting it into another format.*/
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        String newDateString = null;
        try {
            date = dateFormat.parse(textDateTemp);
            SimpleDateFormat dateViewFormat = new SimpleDateFormat("E',' MMM-dd/yyyy 'at' hh:mm:ss aa");
            newDateString = dateViewFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        noteTitle.setText(noteTextTitle);
        noteText.setText(noteTextTemp);

        if (newDateString != null) {
            textDate.setText(newDateString);
        } else {
            textDate.setText(cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CREATED)));
        }

        imgThumbnail.setImageResource(imgThumb1);

        if (cardViewColour != 0){
            cardView.setCardBackgroundColor(cardViewColour);
        } else {
            cardViewColour = -1;
            cardView.setCardBackgroundColor(cardViewColour);
        }

        switch (noteFontSize) {
            case 14: noteText.setTextSize(noteFontSize); break;
            case 18: noteText.setTextSize(noteFontSize); break;
            case 22: noteText.setTextSize(noteFontSize); break;
            default:
                // set font size 18 (medium).
                noteText.setTextSize(18);
        }

        if (noteBodyHidden != 0) {
            noteText.setVisibility(View.GONE);
        } else {
            noteText.setVisibility(View.VISIBLE);
        }
    }

    //new code (for selection highlight)
    public void setNewSelection(int position, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
    }

    public void removeSelection(int position) {
        mSelection.remove(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        mSelection.clear();
        notifyDataSetChanged();
    }

}
