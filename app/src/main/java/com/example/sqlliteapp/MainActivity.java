package com.example.sqlliteapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    DBManager dbManager;

    long RecordID;
    String RecordTitle;
    String RecordDesc;
    String RecordDateRem;
    String RecordTimeRem;
    ArrayList<AdapterItems> listnewsData = new ArrayList<>();
    MyCustomAdapter myadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(),"Welcome back!",Toast.LENGTH_SHORT).show();
        dbManager=new DBManager(this);

        getdatabaseinfo(1,"ignore");
        createNotificationChannel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        SearchView sv=(SearchView)menu.findItem(R.id.menu_search).getActionView();
        SearchManager sm=(SearchManager)getSystemService(Context.SEARCH_SERVICE);
        sv.setSearchableInfo(sm.getSearchableInfo(getComponentName()));
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getdatabaseinfo(2,query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getdatabaseinfo(2,newText);

                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            //Toast.makeText(getApplicationContext(), "Settings is currently under development!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void createNotificationChannel() {
        String CHANNEL_ID="ReminderID";
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{0});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void getdatabaseinfo(int count1,String to_search){


        //add data and view it
        listnewsData.clear();
        //String[] projection={"","",""};
        Cursor cursor;
        if (count1==1) {
            cursor = dbManager.query(null, null, null, DBManager.ColID);
        }
        else{
            String[] SelectionsArgs={"%"+to_search+"%","%"+to_search+"%"};
            cursor = dbManager.query(null, "Title like ? or Description like ?", SelectionsArgs, DBManager.ColID);
        }
        //If wanted to select all data then give null in selection
        if(cursor.moveToFirst()){

            do{
                listnewsData.add(new AdapterItems(cursor.getLong(cursor.getColumnIndex(DBManager.ColID)),
                        cursor.getString(cursor.getColumnIndex(DBManager.ColDateTime)),
                        cursor.getString(cursor.getColumnIndex(DBManager.ColTitle)),
                        cursor.getString(cursor.getColumnIndex(DBManager.ColDescription)),
                        cursor.getString(cursor.getColumnIndex(DBManager.ColRemTime)),
                        cursor.getString(cursor.getColumnIndex(DBManager.ColRemDate))));

            }while (cursor.moveToNext());

        }

        myadapter=new MyCustomAdapter(listnewsData);


        final ListView lsNews=(ListView)findViewById(R.id.lv_all);
        lsNews.setAdapter(myadapter);//intisal with data


    }


    public void update_element_new(){
        // For updating notes
        String title_received=RecordTitle;
        String description_received= RecordDesc;
        String RecordID_string=String.valueOf(RecordID);
        String time_rem_received=RecordTimeRem;
        String date_rem_received=RecordDateRem;
        Intent add_edit_act_intent=new Intent(getApplicationContext(),Main3Activity.class);
        add_edit_act_intent.putExtra("titlefrom",title_received);
        add_edit_act_intent.putExtra("descriptionfrom",description_received);
        add_edit_act_intent.putExtra("add_or_update","UPDATE");
        add_edit_act_intent.putExtra("recordno",RecordID_string);
        add_edit_act_intent.putExtra("rem_time",time_rem_received);
        add_edit_act_intent.putExtra("rem_date",date_rem_received);
        startActivityForResult(add_edit_act_intent,4);
}

    public void bu_add_edit_activity(View view) {
        // For adding new notes


        // Setting recordid to maximum exact size of the row+1
        RecordID=dbManager.RowCount()+1;

        String RecordID_string=String.valueOf(RecordID);
        Intent add_edit_act_intent1=new Intent(getApplicationContext(),Main3Activity.class);
        add_edit_act_intent1.putExtra("titlefrom","ignore");
        add_edit_act_intent1.putExtra("descriptionfrom","ignore");
        add_edit_act_intent1.putExtra("add_or_update","ADD");
        add_edit_act_intent1.putExtra("recordno",RecordID_string);
        add_edit_act_intent1.putExtra("rem_time","ignore");
        add_edit_act_intent1.putExtra("rem_date","ignore");
        startActivityForResult(add_edit_act_intent1,3);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==3)
        {
//            do the things u wanted
            getdatabaseinfo(1, "ignore");
            RecordID=0;
        }
        else if(requestCode==4){
            getdatabaseinfo(1, "ignore");
            RecordID=0;
        }
    }


    private class MyCustomAdapter extends BaseAdapter {
        public ArrayList<AdapterItems> listnewsDataAdpater ;

        public MyCustomAdapter(ArrayList<AdapterItems>  listnewsDataAdpater) {
            this.listnewsDataAdpater=listnewsDataAdpater;
        }


        @Override
        public int getCount() {
            return listnewsDataAdpater.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        TextView txt_datetime_rem, txt_title, txt_desc;
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater mInflater = getLayoutInflater();
            View myView = mInflater.inflate(R.layout.layout_ticket, null);

            final AdapterItems s = listnewsDataAdpater.get(position);


            String rem_DateTime=s.Time+" "+s.Date;
            txt_datetime_rem=(TextView)myView.findViewById(R.id.date_time_id_rem);

            if(s.Time.equalsIgnoreCase("notset")) {
                txt_datetime_rem.setVisibility(View.GONE);
            }
            else {
                txt_datetime_rem.setVisibility(View.VISIBLE);
                txt_datetime_rem.setText(rem_DateTime);
            }
            txt_title=(TextView)myView.findViewById(R.id.title_tv2);
            txt_title.setText(s.Title);

            txt_title.setSelected(true);

            txt_desc=(TextView)myView.findViewById(R.id.desc_tv2);
            txt_desc.setText(s.Description);



            txt_desc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecordID=s.ID;
                    RecordTitle=s.Title;
                    RecordDesc=s.Description;
                    RecordDateRem=s.Date;
                    RecordTimeRem=s.Time;
                    update_element_new();

                }
            });
            return myView;
        }

    }

}
