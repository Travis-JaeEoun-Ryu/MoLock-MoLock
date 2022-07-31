package com.example.molockmolock;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.molockmolock.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;


public class Alram1 extends Activity {
    String myJSON;
    String mDb;
    private static final String TAG_RESULTS = "result";
    private static final String TAG_ID = "ID";
    private static final String TAG_STATE = "STATE";
    private static final String TAG_TIME = "TIME";

    JSONArray peoples = null;
    ArrayList<HashMap<String,String>> personList;
    ListView list;

    

    @Override
    protected void onCreate(Bundle savedlnstanceState){
        super.onCreate(savedlnstanceState);
        setContentView(R.layout.alram1);
        list = (ListView)findViewById(R.id.listView);
        personList=new ArrayList<HashMap<String,String>>();
        getData("http://3.144.90.77/login2.php");
    }


    protected void showList(){
        try{
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);

            for(int i=0; i<peoples.length();i++){
                JSONObject c =peoples.getJSONObject(i);

                String id = c.getString(TAG_ID);
                String state = c.getString(TAG_STATE);
                String time = c.getString(TAG_TIME);
                Log.d("showList : ", id + state + time);

                HashMap<String,String>persons=new HashMap<String,String>();
                persons.put(TAG_ID,id);
                persons.put(TAG_STATE,state);
                persons.put(TAG_TIME,time);

                personList.add(persons);
            }
            ListAdapter adapter = new SimpleAdapter(
                    Alram1.this, personList, R.layout.list_item,
                    new String[]{TAG_ID, TAG_STATE, TAG_TIME},
                    new int[]{R.id.id,R.id.state, R.id.time}
            );
            list.setAdapter(adapter);
        }   catch (JSONException e){
            e.printStackTrace();
        }

    }

    public void getData(String url){
        class GetDataJSON extends AsyncTask<String,Void, String> {
            @Override
            protected String doInBackground(String...params){
                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    return sb.toString().trim();
                } catch(Exception e){
                    return null;
                }

            }
            @Override
            protected void onPostExecute(String result){
                myJSON = result;
                showList();
            }

        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }
}