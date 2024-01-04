package com.example.scheduleplanner.Adapter;


import static com.example.scheduleplanner.R.id.delete_task;
import static com.example.scheduleplanner.R.id.edit_task;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scheduleplanner.AddTaskActivity;
import com.example.scheduleplanner.MainActivity;
import com.example.scheduleplanner.Model.ToDoModel;
import com.example.scheduleplanner.R;
import com.example.scheduleplanner.Utils.DataBase;

import java.util.Calendar;
import java.util.List;


public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private static List<ToDoModel> List;
    private final MainActivity activity;
    private final DataBase db;


    public ToDoAdapter(DataBase db, MainActivity activity) {
        this.db = db;
        this.activity = activity;

    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tasklayout, parent, false);

        return new ViewHolder(item);

    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        ToDoModel i = List.get(position);
        holder.task.setChecked(Boolean.parseBoolean(String.valueOf(i.getStatus())));
        holder.task.setText(i.getTask());
        holder.date.setText(i.getDate());
        holder.time.setText(i.getTime());
        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    mark_finished();

                } else {
                    db.updateStatus(i.getId(), 0);
                }
            }
        });
    }
    private void mark_finished() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Task Completed");
        builder.setMessage("Are you sure you want to mark this Task As Finished?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(activity, "You Clicked Yes", Toast.LENGTH_SHORT).show();


            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(activity, "You Clicked No", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    public int getItemCount() {
        return List.size();
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    public void setTasks(List<ToDoModel> List) {
        ToDoAdapter.List = List;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
        CheckBox task;
        RelativeLayout rl_task;
        TextView date, time;

        DataBase d = new DataBase(itemView.getContext());

        ViewHolder(View v) {
            super(v);
            rl_task = v.findViewById(R.id.rl_task);
            task = v.findViewById(R.id.cb);
            date = v.findViewById(R.id.tv_showdate);
            time = v.findViewById(R.id.tv_showtime);
            rl_task.setOnLongClickListener(this);
        }

        private void showMenu(View v) {
            PopupMenu menu = new PopupMenu(v.getContext(), v);
            menu.inflate(R.menu.longpressmenu);
            menu.setOnMenuItemClickListener(this);
            menu.show();
        }

        @Override
        public boolean onLongClick(View view) {
            showMenu(view);
            return false;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
            final int pos = getAdapterPosition();
            if (menuItem.getItemId() == edit_task) {
                updateItem(pos);
                return true;
            } else if (menuItem.getItemId() == delete_task) {
                d.openDatabase();
                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle("Delete Task");
                builder.setMessage("Are you sure you want to delete this Task?");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(pos);
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return false;
        }
        public void deleteItem(int position) {
            ToDoModel item = List.get(position);
            db.deleteTask(item.getId());
            List.remove(position);
            Toast.makeText(activity, "Task Deleted", Toast.LENGTH_SHORT).show();
            notifyItemRemoved(position);
        }

        public void updateItem(int position) {
            ToDoModel item = List.get(position);
            Bundle bundle = new Bundle();

            bundle.putInt("id", item.getId());
            bundle.putString("task", item.getTask());
            bundle.putString("date", item.getDate());//LLL E
            bundle.putString("time", item.getTime());
            bundle.putInt("status", item.getStatus());
            Intent i = new Intent(itemView.getContext(), AddTaskActivity.class);
            i.putExtras(bundle);

            ContextCompat.startActivity(itemView.getContext(), i, bundle);
        }

    }
}
