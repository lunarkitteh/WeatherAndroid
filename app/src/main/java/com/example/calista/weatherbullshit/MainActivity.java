package com.example.calista.weatherbullshit;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private String OPEN_WEATHER_MAP_API = "f2c045dcc1d66ff9dc1546e9f4098d46";
    private TextView mShowDate,mTemp, mHumid, mPress, mCity, mWeatherIcon, mSelectCity, mDetails, mUpdated;
    Typeface weatherFont;
    String city = "London, GB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // connect the java variables with the xml variables
        mTemp = (TextView) findViewById(R.id.tempertaure);
        mHumid = (TextView) findViewById(R.id.humidity);
        mPress = (TextView) findViewById(R.id.pressure);
        mDetails = (TextView) findViewById(R.id.details);
        mSelectCity = (TextView) findViewById(R.id.selectCity);
        mShowDate = (TextView) findViewById(R.id.textView);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        mShowDate.setText(date);
        mCity = (TextView) findViewById(R.id.city_field);
        mUpdated = (TextView) findViewById(R.id.updated_field);
        mWeatherIcon = (TextView) findViewById(R.id.weather_icon);

        // specify that the weatherFont is taken from the downloaded resource
        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weather-icons-master/font/weathericons-regular-webfont.ttf");
        mWeatherIcon.setTypeface(weatherFont); // set the icons to the downloaded resource

        taskLoadUp(city);

        mSelectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Change City");
                final EditText input = new EditText(MainActivity.this);
                input.setText(city);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Change",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                city = input.getText().toString();
                                taskLoadUp(city);
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });

    }


    public void taskLoadUp(String query) {
        if (Function.isNetworkAvailable(getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }


    class DownloadWeather extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            String xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                    "&units=metric&appid=" + OPEN_WEATHER_MAP_API);
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    DateFormat df = DateFormat.getDateTimeInstance();

                    mCity.setText(json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country"));
                    mDetails.setText(details.getString("description").toUpperCase(Locale.US));
                    mTemp.setText(String.format("%.2f", main.getDouble("temp")) + "°");
                    mHumid.setText("Humidity: " + main.getString("humidity") + "%");
                    mPress.setText("Pressure: " + main.getString("pressure") + " hPa");
                    mHumid.setText(df.format(new Date(json.getLong("dt") * 1000)));
                    mWeatherIcon.setText(Html.fromHtml(Function.setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000)));

                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
            }
        }
    }
}