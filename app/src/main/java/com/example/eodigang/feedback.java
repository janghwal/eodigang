package com.example.eodigang;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class feedback extends AppCompatActivity {
    private static final String TAG = "feedbackactivity";

    TextView textView2;
    Spinner spinner1, spinner2, spinner3;
    List<String> placeList1, placeList2, placeList3;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        textView2 = findViewById(R.id.textView2);
        spinner1 = findViewById(R.id.spinner);
        spinner2 = findViewById(R.id.spinner2);
        spinner3 = findViewById(R.id.spinner3);
        button = findViewById(R.id.button);

        placeList1 = new ArrayList<>();
        placeList2 = new ArrayList<>();
        placeList3 = new ArrayList<>();

        // AsyncTask를 사용하여 첫 번째 스피너 데이터 읽기
        new DownloadTask(1, null, null, null, null, null).execute();

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedPlace1 = placeList1.get(position);

                // AsyncTask를 사용하여 두 번째 스피너 데이터 읽기
                new DownloadTask(2, selectedPlace1, null, null, null, null).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 아무것도 선택되지 않았을 때의 동작
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedPlace1 = placeList1.get(spinner1.getSelectedItemPosition());
                String selectedPlace2 = placeList2.get(position);

                // AsyncTask를 사용하여 세 번째 스피너 데이터 읽기
                new DownloadTask(3, selectedPlace1, selectedPlace2, null, null, null).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 아무것도 선택되지 않았을 때의 동작
            }
        });

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedPlace1 = placeList1.get(spinner1.getSelectedItemPosition());
                String selectedPlace2 = placeList2.get(spinner2.getSelectedItemPosition());
                String selectedPlace3 = placeList3.get(position);

                textView2.setText(selectedPlace1 + " " + selectedPlace2 + " " + selectedPlace3);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 아무것도 선택되지 않았을 때의 동작
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedPlace1 = placeList1.get(spinner1.getSelectedItemPosition());
                String selectedPlace2 = placeList2.get(spinner2.getSelectedItemPosition());
                String selectedPlace3 = placeList3.get(spinner3.getSelectedItemPosition());
                String weekNum = getCurrentDayOfWeek();
                String currentTime = getCurrentTime();
                Log.v(TAG, "서버에 보낼 데이터: " + weekNum);
                Log.v(TAG, "서버에 보낼 데이터: " + currentTime);

                new DownloadTask(4, selectedPlace1, selectedPlace2, selectedPlace3, weekNum, currentTime).execute();

            }
        });
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

    private class DownloadTask extends AsyncTask<Void, Void, String> {
        private int spinnerNumber;
        private String place1;
        private String place2;
        private String place3;
        private String week_num;
        private String TIME;

        public DownloadTask(int spinnerNumber, String place1, String place2, String place3, String week_num, String TIME) {
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
                } else if (spinnerNumber == 3){
                    urlString = "http://10.0.2.2/findplace3.php?place1=" + place1 + "&place2=" + place2;
                } else{
                    Log.v(TAG, "서버"+place1+place2+place3+week_num+TIME);
                    urlString = "http://10.0.2.2/feedback.php?place1=" + place1 + "&place2=" + place2 + "&place3=" + place3 + "&week_num=" + week_num + "&TIME=" + TIME;
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
                    Log.v(TAG, "서버 "+ result );
                } else {
                    Log.e(TAG, "서버 연결 실패, 응답 코드: " + responseCode);
                }

                // 연결 종료
                connection.disconnect();

            } catch (IOException e) {
                Log.e(TAG, "IOException 발생: " + e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Log.d(TAG, "서버에서 받은 데이터: " + result);
                displayResult(result);
            } else {
                Log.e(TAG, "서버에서 데이터를 읽어오지 못했습니다.");
            }
        }

        private void displayResult(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);

                if (spinnerNumber == 1) {
                    placeList1.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String place = jsonObject.getString("place1");
                        placeList1.add(place);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(feedback.this, android.R.layout.simple_spinner_item, placeList1);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner1.setAdapter(adapter);
                } else if (spinnerNumber == 2) {
                    placeList2.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String place = jsonObject.getString("place2");
                        placeList2.add(place);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(feedback.this, android.R.layout.simple_spinner_item, placeList2);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner2.setAdapter(adapter);
                } else if (spinnerNumber == 3) {
                    placeList3.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String place = jsonObject.getString("place3");
                        placeList3.add(place);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(feedback.this, android.R.layout.simple_spinner_item, placeList3);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner3.setAdapter(adapter);
                } else {

                }
            } catch (JSONException e) {
                Log.e(TAG, "서버 JSON 파싱 에러: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "업데이트", Toast.LENGTH_LONG).show();
            }
        }
    }
}
