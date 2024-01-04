package com.example.scheduleplanner;

import static java.nio.file.Files.find;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.scheduleplanner.Adapter.ToDoAdapter;
//import com.example.scheduleplanner.Brodcast.AlarmReciver;
import com.example.scheduleplanner.Model.ToDoModel;
import com.example.scheduleplanner.Utils.DataBase;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;

public class AddTaskActivity extends AppCompatActivity {
    EditText et_date, et_task,et_time,et_id;
    TextView tv_time;
    ImageView clock;
    Button btn_addtask;
    final String CHANNEL_ID = "todoreminder";
    NotificationChannel channel;
    AlarmManager a;
    PendingIntent p;
     private ToDoAdapter adapter;
    int cyear, cmon, cday, chour, cmin;
    long cdate;
    Calendar c;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        find();

        Calendar c = Calendar.getInstance();
        cyear = c.get(Calendar.YEAR);
        cmon = c.get(Calendar.MONTH);
        cday = c.get(Calendar.DAY_OF_MONTH);
        chour = c.get(Calendar.HOUR_OF_DAY);
        cmin = c.get(Calendar.MINUTE);
        cmon = cmon + 1;
        cdate = c.getTimeInMillis();

        DataBase db = new DataBase(this);
        db.openDatabase();
        final Bundle bundle = getIntent().getExtras();
        // cdate= Long.parseLong(String.valueOf(cday)+"/"+String.valueOf(cmon)+"/"+String.valueOf(cyear));
        //Toast.makeText(AddTaskActivity.this,cdate,Toast.LENGTH_LONG).show();
        et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog dp = new DatePickerDialog(AddTaskActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int mon, int day) {
                                                   //datePicker.setMinDate(cdate-1000);
                        et_date.setText(day + "/" + mon + "/" + year);
                        tv_time.setVisibility(View.VISIBLE);
                        et_time.setVisibility(View.VISIBLE);


                                                   //

                                                   //tp.setHour((int) System.currentTimeMillis()-1000);
                                                   //tp.setMinute((int) System.currentTimeMillis()-1000);

                        }
                    }, cyear - 1000, cmon - 1000, cday - 1000);
                dp.getDatePicker().setMinDate(cdate - 1000);
                dp.show();
            }
        });

        et_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog tp=new TimePickerDialog(AddTaskActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int min) {
                        if(hour>12){
                            int h=hour-12;

                            c.set(Calendar.HOUR,h);
                            c.set(Calendar.MINUTE,min);
                            if(c.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()){
                                //Toast.makeText(AddTaskActivity.this," wrong time  ", Toast.LENGTH_SHORT).show();

                                Snackbar s=Snackbar.make( view,"Please select future time only",Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                            }
                                        });
                                s.show();
                            }
                            else{
                                et_time.setText(h+" : "+min+" PM");
                            }
                            //clock.setVisibility(View.VISIBLE);
                            //setAlarm();
                        }
                        else{
                            if(c.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()){
                                Snackbar s=Snackbar.make( view,"Please select future time only",Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                    }
                                });
                                s.show();                            }
                            else {
                                et_time.setText(hour + " : " + min + " AM");
                                c.set(Calendar.HOUR, hour);
                                c.set(Calendar.MINUTE, min);
                                //clock.setVisibility(View.VISIBLE);
                            }
                        }

                    }

                },chour,cmin,false);
                tp.show();
            }
        });

        btn_addtask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    ToDoModel task=new ToDoModel();
                    task.setTask(et_task.getText().toString());
                    task.setDate(et_date.getText().toString());
                    task.setTime(et_time.getText().toString());

                    if (et_task.getText().toString().isEmpty())
                    {
                        Toast.makeText(getApplicationContext(), "Task Required", Toast.LENGTH_LONG).show();

                    }
                    else {

                        db.insertTask(task);
                        Intent i=new Intent(AddTaskActivity.this,MainActivity.class);
                        startActivity(i);

                    }
        }
        });
        if(bundle!=null)
        {
            int id;
            tv_time.setVisibility(View.VISIBLE);
            et_time.setVisibility(View.VISIBLE);
            id=bundle.getInt("id");

            et_task.setText(bundle.getString("task","not getting"));
            et_date.setText(bundle.getString("date","not getting"));
            et_time.setText(bundle.getString("time","not getting"));
            if(et_time.getText().toString()==""){
                et_time.setText("select time");

            }

            btn_addtask.setText("SAVE CHANGES");

            btn_addtask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String update_task ,update_date,update_time;
                    update_task=et_task.getText().toString();
                    update_date=et_date.getText().toString();
                    update_time=et_time.getText().toString();
                    db.updateTask(id,update_task,update_date,update_time);
                    Toast.makeText(AddTaskActivity.this, "Task Updated", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void find() {
        et_date = findViewById(R.id.et_date);
        et_task = findViewById(R.id.et_task);
        et_time=findViewById(R.id.et_time);
        et_id=findViewById(R.id.et_id);
        tv_time = findViewById(R.id.tv_time);
        btn_addtask = findViewById(R.id.btn_addtask);
        //tp = findViewById(R.id.tp);
        //clock=findViewById(R.id.clock);
    }
}