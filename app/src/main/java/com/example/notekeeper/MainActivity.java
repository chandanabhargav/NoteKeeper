package com.example.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String NOTE_POSITION = "com.example.notekeeper.NOTE_POSITION";
    public static final String ORIGINAL_COURSE_ID = "com.example.notekeeper.COURSE_ID";
    public static final String ORIGINAL_TITLE = "com.example.notekeeper.TITLE";
    public static final String ORIGINAL_TEXT = "com.example.notekeeper.TEXT";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean ismNewNote;
    private Spinner spinner_courses;
    private EditText textNoteTitle;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_COURSE_ID, originalCourseId);
        outState.putString(ORIGINAL_TITLE, originalTitle);
        outState.putString(ORIGINAL_TEXT, originaltext);
    }

    private EditText textNoteText;
    private boolean mIsCancelling;
    private int newNotePosition;
    private String originalCourseId;
    private String originalTitle;
    private String originaltext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinner_courses = (Spinner) findViewById(R.id.spinner_courses);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<CourseInfo>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_courses.setAdapter(adapterCourses);

        readDisplayStateValues();

        if(savedInstanceState == null)
            saveOriginalCourseValue();
        else
            restoreOriginalValue(savedInstanceState);


        textNoteTitle = (EditText) findViewById(R.id.text_note_title);
        textNoteText = (EditText) findViewById(R.id.text_note_text);

        if(!ismNewNote)
            displayNote(spinner_courses, textNoteTitle, textNoteText);
    }

    private void restoreOriginalValue(Bundle savedInstanceState) {
        originalCourseId = savedInstanceState.getString(ORIGINAL_COURSE_ID);
        originalTitle = savedInstanceState.getString(ORIGINAL_TITLE);
        originaltext = savedInstanceState.getString(ORIGINAL_TEXT);
    }

    private void saveOriginalCourseValue() {
        if(ismNewNote)
            return;

        originalCourseId = mNote.getCourse().getCourseId();
        originalTitle = mNote.getTitle().toString();
        originaltext = mNote.getText().toString();
    }

    private void displayNote(Spinner spinner_courses, EditText textNoteTitle, EditText textNoteText) {

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinner_courses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValues() {

        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        ismNewNote = position == POSITION_NOT_SET;
        if(ismNewNote) {
            createNote();
        }
        else {
            mNote = DataManager.getInstance().getNotes().get(position);
        }
    }

    public void createNote() {
        DataManager dm = DataManager.getInstance();
        newNotePosition = dm.createNewNote();
        mNote = dm.getNotes().get(newNotePosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendMail();
            return true;
        }
        else if(id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling) {
            if(ismNewNote) {
                DataManager.getInstance().removeNote(newNotePosition);
            }
            else {
                storeOriginalValue();
            }
        }
        else {
            saveNote();
        }
    }

    private void storeOriginalValue() {
        CourseInfo course = DataManager.getInstance().getCourse(originalCourseId);
        mNote.setCourse(course);
        mNote.setTitle(originalTitle);
        mNote.setText(originaltext);
    }

    public void saveNote() {
        mNote.setCourse((CourseInfo) spinner_courses.getSelectedItem());
        mNote.setTitle(textNoteTitle.getText().toString());
        mNote.setText(textNoteText.getText().toString());
    }

    public void sendMail() {
        CourseInfo course = (CourseInfo) spinner_courses.getSelectedItem();
        String text = "Check out what i learned at Pluralsight\n"+ course.getTitle() +"\n"+
            textNoteTitle.getText().toString();
        String subject = textNoteTitle.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }
}
