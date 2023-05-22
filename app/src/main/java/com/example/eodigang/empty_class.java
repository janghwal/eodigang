package com.example.eodigang;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class empty_class extends AppCompatActivity {
    private static final String empty_TAG = "empty_class_activity";
    TextView building, room;
    Spinner spin_place1, spin_place2;
    List<String> list_place1, list_place2, list_place3, endtimeList;
    Button serch_btn;
    ListView empty_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_class);

        building = findViewById(R.id.building);
        room = findViewById(R.id.room);
        spin_place1 = findViewById(R.id.place1);
        spin_place2 = findViewById(R.id.place2);
        serch_btn = findViewById(R.id.serch_btn);
        empty_list = findViewById(R.id.empty_list);

        list_place1 = new ArrayList<>();
        list_place2 = new ArrayList<>();
        list_place3 = new ArrayList<>();
        endtimeList = new ArrayList<>();

        new DownloadTask1(1, null, null, null, null, null).execute();

        spin_place1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedPlace1 = list_place1.get(position);

                // AsyncTask를 사용하여 두 번째 스피너 데이터 읽기
                new DownloadTask1(2, selectedPlace1, null, null, null, null).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 아무것도 선택되지 않았을 때의 동작
            }
        });

    }

    private class DownloadTask1 extends AsyncTask<Void, Void, String> {
        private int spinnerNumber;
        private String place1;
        private String place2;
        private String place3;
        private String week_num;
        private String TIME;

        public DownloadTask1(int spinnerNumber, String place1, String place2, String place3, String week_num, String TIME) {
            this.spinnerNumber = spinnerNumber;
            this.place1 = place1;
            this.place2 = place2;
            this.place3 = place3;
            this.week_num = week_num;
            this.TIME = TIME;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            try {
                String urlString;
                if (spinnerNumber == 1) {
                    urlString = "http://10.0.2.2/findplace1.php";
                } else if (spinnerNumber == 2) {
                    urlString = "http://10.0.2.2/findplace2.php?place1=" + place1;
                } else {
                    urlString = "http://10.0.2.2/findplace3.php?place1=" + place1 + "&place2=" + place2;
                }
                // 서버 URL 설정
                URL url = new URL(urlString);

                // HTTP 연결 생성
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // 연결 설정
                connection.setRequestMethod("GET");

                // 응답 코드 확인
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 서버로부터 데이터 읽기
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();
                    inputStream.close();

                    result = stringBuilder.toString();
                    Log.v(empty_TAG, "서버 "+ result );
                } else {
                    Log.e(empty_TAG, "서버 연결 실패, 응답 코드: " + responseCode);
                }

                // 연결 종료
                connection.disconnect();

            } catch (IOException e) {
                Log.e(empty_TAG, "IOException 발생: " + e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Log.d(empty_TAG, "서버에서 받은 데이터: " + result);
                displayResult(result);
            } else {
                Log.e(empty_TAG, "서버에서 데이터를 읽어오지 못했습니다.");
            }
        }

        private void displayResult(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);

                if (spinnerNumber == 1) {
                    list_place1.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String place = jsonObject.getString("place1");
                        list_place1.add(place);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(empty_class.this, android.R.layout.simple_spinner_item, list_place1);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spin_place1.setAdapter(adapter);
                } else if (spinnerNumber == 2) {
                    list_place2.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String place = jsonObject.getString("place2");
                        list_place2.add(place);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(empty_class.this, android.R.layout.simple_spinner_item, list_place2);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spin_place2.setAdapter(adapter);
                } else {

                }
            } catch (JSONException e) {
                Log.e(empty_TAG, "서버 JSON 파싱 에러: " + e.getMessage());
                //Toast.makeText(getApplicationContext(), "업데이트", Toast.LENGTH_LONG).show();
            }
        }
    }
}