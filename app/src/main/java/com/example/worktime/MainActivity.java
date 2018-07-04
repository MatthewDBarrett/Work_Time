package com.example.worktime;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    int totalMinutes;

    ArrayList<String> times = new ArrayList<>();
    ArrayList<String> mKeys = new ArrayList<>();

    TextView textView ;
    TextView moneyView;
    TextView totalTimeView;

    Button start, pause, reset, lap, clear, add ;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;

    Handler handler;

    int Seconds, Minutes, MilliSeconds, Hours ;

    ListView listView ;

    String[] ListElements = new String[] {  };

    List<String> ListElementsArrayList ;

    ArrayAdapter<String> adapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        textView = (TextView)findViewById(R.id.textView);
        moneyView = (TextView)findViewById(R.id.moneyView);
        totalTimeView = (TextView)findViewById(R.id.totalTimeView);

        start = (Button)findViewById(R.id.button);
        pause = (Button)findViewById(R.id.button2);
        reset = (Button)findViewById(R.id.button3);
        lap = (Button)findViewById(R.id.button4) ;
        clear = (Button)findViewById(R.id.button5) ;
        add = (Button)findViewById(R.id.button6) ;
        listView = (ListView)findViewById(R.id.listview1);

        handler = new Handler() ;

//        loadData();

        adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, times) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.WHITE);

                // Generate ListView Item using TextView
                return view;
            }
        };
        listView.setAdapter(adapter);

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getValue(String.class);
                times.add( String.valueOf( Integer.valueOf(value)/60 ) + "h " + String.valueOf( Integer.valueOf(value)%60 ) + "m" );
                String key = dataSnapshot.getKey();
                mKeys.add(key);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getValue(String.class);
                String key = dataSnapshot.getKey();
                int index = mKeys.indexOf(key);
                times.set(index, String.valueOf( Integer.valueOf(value)/60 ) + "h " + String.valueOf( Integer.valueOf(value)%60 ) + "m" );
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int index = mKeys.indexOf(key);
                times.remove( index );
                mKeys.remove( index );
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StartTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);

                reset.setEnabled(false);

            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimeBuff += MillisecondTime;

                handler.removeCallbacks(runnable);

                reset.setEnabled(true);

            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MillisecondTime = 0L;
                StartTime = 0L;
                TimeBuff = 0L;
                UpdateTime = 0L;
                Seconds = 0;
                Minutes = 0;
                MilliSeconds = 0;

                textView.setText("0h 0m 00s");
                moneyView.setText("$0.00");

//                ListElementsArrayList.clear();

//                adapter.notifyDataSetChanged();

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clearData();
                totalTimeView.setText( "0h 0m" );

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int minute = 60000;

                TimeBuff += minute * 10;

            }
        });

        lap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( Seconds > 0 ) {

                    saveData();

                }

            }
        });

    }

    private void saveData() {
//        times.add( String.valueOf(Hours) + "h " + String.valueOf(Minutes) + "m" );
//        adapter.notifyDataSetChanged();

        totalMinutes += UpdateTime;
        int time = (int) (totalMinutes / 60000);
        totalTimeView.setText( "" + time/60 + "h " + time%60 + "m " );

//        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(times);
//        editor.putString("task list", json);
//        editor.putInt("total time", totalMinutes);
//        editor.apply();

        mDatabase.child( String.valueOf( times.size() ) ).setValue( String.valueOf(UpdateTime / 60000) ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Stored", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error: Data not saved", Toast.LENGTH_LONG).show();
                }
            }
        });

//        HashMap<String, String> dataMap = new HashMap<String, String>();
//        dataMap.put("Name", "Matthew");
//        dataMap.put("Email", "matthew_d_barrett@hotmail.com");
//
//        mDatabase.push().setValue( dataMap ).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//                if(task.isSuccessful()){
//                    Toast.makeText(MainActivity.this, "Stored", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "Error: Data not saved", Toast.LENGTH_LONG).show();
//                }
//
//            }
//        });
    }

//    private void loadData() {
//        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
//        Gson gson = new Gson();
//        String json = sharedPreferences.getString("task list", null);
//        Type type = new TypeToken<ArrayList<String>>() {}.getType();
//        times = gson.fromJson(json, type);
//
//        int tempTotTime = sharedPreferences.getInt("total time", -1);
//        int time = (int) (tempTotTime / 60000);
//        totalTimeView.setText( "" + time/60 + "h " + time%60 + "m " );
//
//        if (times == null){
//            times = new ArrayList<>();
//        }
//
//    }

    private void clearData() {
        for (int i = 0; i < mKeys.size(); i++)
            mDatabase.child( mKeys.get( i ) ).removeValue();
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            int secondTime = (int) (UpdateTime / 1000);

            Seconds = secondTime % 60;

            int time = (int) (UpdateTime / 60000);

            Hours = time / 60;

            Minutes = time % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

//            textView.setText("" + Minutes + ":"
//                    + String.format("%02d", Seconds) + ":"
//                    + String.format("%03d", MilliSeconds));

            textView.setText("" + Hours + "h " + Minutes + "m "
                    + String.format("%02d", Seconds) + "s" );

            int moneyTime = (int) (UpdateTime / 1800);

            int dollars = moneyTime / 100;
            int cents = moneyTime % 100;

            moneyView.setText("" + "$"+ dollars + "." + cents );

//            totalTime = "" + Hours + "h " + Minutes + "m ";
//            totalTimeView.setText( totalTime );

            handler.postDelayed(this, 0);
        }

    };

}