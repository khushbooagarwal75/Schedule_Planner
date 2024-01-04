package com.example.scheduleplanner.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.scheduleplanner.Model.ToDoModel;

import java.util.ArrayList;
import java.util.List;

public class DataBase extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String NAME = "toDoList";
    private static final String TODO_TABLE = "todo";
    private static final String TASK_FINISHED_TABLE = "TaskFinished";
//    'private static final String FID = "fid";'
    private static final String ID = "id";

    private static final String TASK = "task";
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String STATUS = "status";
    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK + " TEXT, "
            + DATE + " TEXT, "+ TIME + " TEXT, " + STATUS + " INTEGER)";
    

    private SQLiteDatabase db;

    public DataBase(Context context) {
        super(context, NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TASK_FINISHED_TABLE);
        onCreate(sqLiteDatabase);
    }
    public void openDatabase() {
        db = this.getWritableDatabase();
    }
    public void insertTask(ToDoModel t){

        ContentValues cv = new ContentValues();
        cv.put(TASK, t.getTask());
        cv.put(DATE,t.getDate());
        cv.put(TIME,t.getTime());
        cv.put(STATUS, 0);
        db.insert(TODO_TABLE, null, cv);
    }
    public void insertTaskFinished(ToDoModel t){
        ContentValues cv = new ContentValues();
        cv.put(ID,t.getId());
        cv.put(TASK, t.getTask());
        cv.put(DATE,t.getDate());
        cv.put(TIME,t.getTime());
        db.insert(TASK_FINISHED_TABLE, null, cv);
    }
    @SuppressLint("Range")
    public List<ToDoModel> getAllTasks(){
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        db.beginTransaction();
        try{
            cur = db.query(TODO_TABLE, null, null, null, null, null, null, null);
            if(cur != null){
                if(cur.moveToFirst()){
                    do{
                        ToDoModel task = new ToDoModel();
                        task.setId(cur.getInt(cur.getColumnIndex(ID)));
                        task.setTask(cur.getString(cur.getColumnIndex(TASK)));
                        task.setDate(cur.getString(cur.getColumnIndex(DATE)));
                        task.setTime(cur.getString(cur.getColumnIndex(TIME)));
                        task.setStatus(cur.getInt(cur.getColumnIndex(STATUS)));
                        taskList.add(task);
                    }
                    while(cur.moveToNext());
                }
            }
        }
        finally {
            db.endTransaction();
            assert cur != null;
            cur.close();
        }
        return taskList;


    }

    public void updateStatus(int id, int status){
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void updateTask(int id, String task,String date,String time) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task);
        cv.put(DATE, date);
        cv.put(TIME, time);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void deleteTask(int id){
        db.delete(TODO_TABLE, ID + "= ?", new String[] {String.valueOf(id)});

    }


}

