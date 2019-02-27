package np.com.rahulrajbanshi.rnotes.model;

public class NoteModel {

    private String noteTitle;
    private String noteText;

    private String noteCreated;
    private int colour, fontSize, bodyHidden;

    public NoteModel(){
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getNoteCreated() {
        return noteCreated;
    }

    public void setNoteCreated(String noteCreated) {
        this.noteCreated = noteCreated;
    }

    public int getColour() {
        return colour;
    }

    public void setColour(int colour) {
        if (colour == 0) {
            this.colour = -1; //setting default colour white
        } else {
            this.colour = colour;
        }
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        switch (fontSize) {
            case 14: this.fontSize = fontSize; break;
            case 18: this.fontSize = fontSize; break;
            case 22: this.fontSize = fontSize; break;
            default: this.fontSize = 18;
        }
    }

    public int getBodyHidden() {
        return bodyHidden;
    }

    public void setBodyHidden(int bodyHidden) {
        this.bodyHidden = bodyHidden;
    }
}
