package com.example.eodigang;

import android.content.Context;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class empty_class extends AppCompatActivity {
    private static final String empty_TAG = "empty_class_activity";
    TextView building, room;
    Spinner spin_place1, spin_place2;
    List<String> list_place1, list_place2, list_place3, endtimeList;
    Button serch_btn, input_timetable;
    ListView empty_list;
    public String main_place1, main_place2;

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
        input_timetable = findViewById(R.id.input_timetable);

        list_place1 = new ArrayList<>();
        list_place2 = new ArrayList<>();
        list_place3 = new ArrayList<>();
        endtimeList = new ArrayList<>();
        String main_weekNum = getCurrentDayOfWeek();
        String main_currentTime = getCurrentTime();

        new DownloadTask1(1, null, null, null, null, null, null, null).execute();
        // 파일에서 데이터 가져오기
        List<String> pppinfo = readDataFromFile1();

        //데이터 처리하고 추천 알고리즘 넣기

        if (0 >= pppinfo.size()) {
            // 데이터가 없는 경우
            building.setVisibility(View.GONE);
            room.setVisibility(View.GONE);
            input_timetable.setVisibility(View.VISIBLE);
            input_timetable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 버튼 클릭 시 처리할 로직 작성
                    Intent intent = new Intent(empty_class.this, timetable.class);
                    startActivity(intent);
                }
            });
        } else {
            String[] ppp = pppinfo.get(0).split(",");
            // 데이터가 있는 경우
            building.setVisibility(View.VISIBLE);
            room.setVisibility(View.VISIBLE);
            input_timetable.setVisibility(View.GONE);
            new DownloadTask1(5, null, null, null, main_weekNum, main_currentTime, ppp[0], ppp[1]).execute();
        }

        spin_place1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String choice1 = list_place1.get(position);

                // AsyncTask를 사용하여 두 번째 스피너 데이터 읽기
                new DownloadTask1(2, choice1, null, null, null, null, null, null).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 아무것도 선택되지 않았을 때의 동작
            }
        });

        serch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String choice1 = list_place1.get(spin_place1.getSelectedItemPosition());
                String choice2 = list_place2.get(spin_place2.getSelectedItemPosition());
                String weekNum = getCurrentDayOfWeek();
                String currentTime = getCurrentTime();
                Log.v(empty_TAG, "서버에 보낼 데이터: " + weekNum);
                Log.v(empty_TAG, "서버에 보낼 데이터: " + currentTime);

                new DownloadTask1(3, choice1, choice2, null, weekNum, currentTime, null, null).execute();

            }
        });

    }

    private List<String> readDataFromFile1() {
        List<String> lines = new ArrayList<>();
        try {
            FileInputStream fileInputStream = openFileInput("FILE_NAME");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    private void writeDataToFile(String data) {
        try {
            FileOutputStream fileOutputStream = openFileOutput("FILE_NAME", Context.MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        return sdf.format(calendar.getTime());
    }

    private String getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Calendar의 DAY_OF_WEEK 상수는 일요일(1)부터 시작하므로, 인덱스를 맞추기 위해 1을 빼줍니다.
        dayOfWeek--;

        // 요일 목록
        String[] daysOfWeek = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};

        // 요일 목록에서 현재 요일에 해당하는 문자열 반환
        return daysOfWeek[dayOfWeek];
    }

    private class DownloadTask1 extends AsyncTask<Void, Void, String> {
        private int url_num;
        private String place1;
        private String place2;
        private String place3;
        private String week_num;
        private String TIME;
        private String course_name;
        private String class_num;

        public DownloadTask1(int url_num, String place1, String place2, String place3, String week_num, String TIME, String course_name, String class_num) {
            this.url_num = url_num;
            this.place1 = place1;
            this.place2 = place2;
            this.place3 = place3;
            this.week_num = week_num;
            this.TIME = TIME;
            this.course_name = course_name;
            this.class_num = class_num;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            try {
                String urlString;
                if (url_num == 1) {
                    urlString = "http://10.0.2.2/findplace1.php";
                } else if (url_num == 2) {
                    urlString = "http://10.0.2.2/findplace2.php?place1=" + place1;
                } else if (url_num == 5){
                    urlString = "http://10.0.2.2/ppp.php?course_name=" + course_name +"&class_num=" + class_num + "&week_num=" + week_num + "&TIME=" + TIME;
                } else {
                    urlString = "http://10.0.2.2/emptyserch.php?place1=" + place1 + "&place2=" + place2 + "&week_num=" + week_num + "&TIME=" + TIME;
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

                if (url_num == 1) {
                    list_place1.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String place = jsonObject.getString("place1");
                        list_place1.add(place);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(empty_class.this, android.R.layout.simple_spinner_item, list_place1);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spin_place1.setAdapter(adapter);
                } else if (url_num == 2) {
                    list_place2.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String place = jsonObject.getString("place2");
                        list_place2.add(place);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(empty_class.this, android.R.layout.simple_spinner_item, list_place2);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spin_place2.setAdapter(adapter);
                } else if (url_num == 3){
                    endtimeList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String place3 = jsonObject.getString("place3");
                        String endtime = jsonObject.getString("end_empty");
                        String place = place3 + "   " + endtime;
                        endtimeList.add(place);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(empty_class.this, android.R.layout.simple_list_item_1, endtimeList);
                    empty_list.setAdapter(adapter);
                } else {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    main_place1 = jsonObject.getString("place1");
                    main_place2 = jsonObject.getString("place2");
                    String main_String1 = main_place1 + "   " + main_place2;
                    building.setText(main_String1);
                    String main_place = jsonObject.getString("place3");
                    String main_endtime = jsonObject.getString("end_empty");
                    String main_String = main_place + "   " + main_endtime;
                    room.setText(main_String);
                }
            } catch (JSONException e) {
                Log.e(empty_TAG, "서버 JSON 파싱 에러: " + e.getMessage());
                //Toast.makeText(getApplicationContext(), "업데이트", Toast.LENGTH_LONG).show();
            }
        }
    }
}