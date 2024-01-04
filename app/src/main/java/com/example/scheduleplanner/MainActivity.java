package com.example.scheduleplanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scheduleplanner.Adapter.ToDoAdapter;
//import com.example.scheduleplanner.Brodcast.AlarmReciver;
import com.example.scheduleplanner.Model.ToDoModel;
import com.example.scheduleplanner.Utils.DataBase;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fb_add;
    private RecyclerView rv_list;
    ImageView ivscan;
    ImageButton ib_mic;
    EditText et_addtask;
    private DataBase db;
    private ToDoAdapter taskadapter;
    private List<ToDoModel> tasklist;
    private Uri imageuri=null;

    private static final String TAG="SCAN";

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=101;

    //Calendar c;
    private String[] cameraper;
    private String[] storageper;
    private ProgressDialog pd;
    
    private TextRecognizer txtrec;
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== 111& resultCode==RESULT_OK){
            et_addtask.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
            ToDoModel task=new ToDoModel();
            task.setTask(et_addtask.getText().toString());


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Task Completed");
            builder.setMessage(" want to add this task?: "+et_addtask.getText());
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    db.insertTask(task);
                    Intent i=new Intent(MainActivity.this,MainActivity.class);
                    startActivity(i);
                    Toast.makeText(MainActivity.this, "Task Added", Toast.LENGTH_SHORT).show();


                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    et_addtask.setText("");
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();



        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        find();
        
        db = new DataBase(this);
        db.openDatabase();

        cameraper=new  String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storageper=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        pd=new ProgressDialog(this);
//        pd.setTitle("plz wiat");
//        pd.setCanceledOnTouchOutside(false);



        rv_list.setLayoutManager(new LinearLayoutManager(this));
        taskadapter = new ToDoAdapter(db, MainActivity.this);
        rv_list.setAdapter(taskadapter);
        tasklist = db.getAllTasks();

        taskadapter.setTasks(tasklist);
        taskadapter.notifyDataSetChanged();

        fb_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(i);
            }
        });

        ib_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,5);
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"start speaking..");
                startActivityForResult(i,111);

            }
        });

        ivscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDialog();
//                if(imageuri==null){
//                    Toast.makeText(MainActivity.this, "pick image first", Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    recognizeTextForImage();
//
//                }
            }
        });
    }

    private void recognizeTextForImage() {
        pd.setMessage("Recognizing image..");
        pd.show();

        try{
            InputImage i=InputImage.fromFilePath(this,imageuri);
            
            pd.setMessage("Recognizing text..");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showImageDialog() {
        PopupMenu pm=new PopupMenu(this,ivscan);
        pm.getMenu().add(Menu.NONE,1,1,"CAMERA");
        pm.getMenu().add(Menu.NONE,2,2,"GALLERY");

        pm.show();

        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id=menuItem.getItemId();
                if(id==1){
                    if(cameraper()){
                        pickimage_camera();
                    }
                    else {
                        reqcameraper();
                    }
                }
                else if(id==2){
                    if(storageper()){
                        pickimg_gallery();
                    }
                    else{
                        reqstorageper();
                    }
                }
                return true;

            }
        });

    }
    private  void pickimg_gallery(){
        Intent i=new Intent(Intent.ACTION_PICK);
        i.setType("kimage/*");
        gallery.launch(i);
    }

    private ActivityResultLauncher<Intent> gallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Intent d=result.getData();
                        imageuri=d.getData();

                    }
                    else {
                        Toast.makeText(MainActivity.this, "no image selected ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private  void  pickimage_camera(){
        ContentValues v=new ContentValues();
        v.put(MediaStore.Images.Media.TITLE,"SAMPLE TITLE");
        v.put(MediaStore.Images.Media.DESCRIPTION,"DESCRIPTION");

        imageuri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);

        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT,imageuri);

        cameraActivity.launch(i);
    }

    private ActivityResultLauncher<Intent> cameraActivity =registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()== Activity.RESULT_OK){
                    }
                    else {
                        Toast.makeText(MainActivity.this, "no image clicked ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean storageper(){
        boolean res=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return res;
    }
    private  void reqstorageper(){
        ActivityCompat.requestPermissions(this,storageper,STORAGE_REQUEST_CODE);
    }

    private boolean cameraper(){
        boolean Cres=ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean sres=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return Cres && sres;
    }
    private  void reqcameraper(){
        ActivityCompat.requestPermissions(this,cameraper,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        pickimage_camera();
                    } else {
                        Toast.makeText(this, "camera and storage permission required", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
                }
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted) {
                        pickimg_gallery();
                    } else {
                        Toast.makeText(this, "storage permission required", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void find() {
        fb_add = findViewById(R.id.fb_add);
        rv_list = findViewById(R.id.rv_list);
        ib_mic = findViewById(R.id.ib_mic);
        et_addtask=findViewById(R.id.ed_addtask);
        ivscan=findViewById(R.id.ivscan);
    }
}