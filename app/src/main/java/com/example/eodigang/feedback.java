package com.example.eodigang;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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

public class feedback extends AppCompatActivity {
    TextView textView2;
    Spinner spinner;
    List<String> placeList;
    private static final String TAG = "feedbackactivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        textView2 = findViewById(R.id.textView2);
        spinner = findViewById(R.id.spinner);

        placeList = new ArrayList<>();

        // AsyncTask를 사용하여 서버로부터 데이터 읽기
        new DownloadTask().execute();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedPlace = placeList.get(position);
                textView2.setText(selectedPlace);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 아무것도 선택되지 않았을 때의 동작
            }
        });
    }

    private class DownloadTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            try {
                // 서버 URL 설정
                URL url = new URL("http://10.0.2.2/findplace1.php");

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

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String place = jsonObject.getString("place1");
                    placeList.add(place);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(feedback.this, android.R.layout.simple_spinner_item, placeList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

            } catch (JSONException e) {
                Log.e(TAG, "JSON 파싱 에러: " + e.getMessage());
            }
        }
    }
}
