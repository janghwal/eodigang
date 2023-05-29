package com.example.eodigang;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

public class timetable extends AppCompatActivity {
    EditText et_course;
    EditText et_section;
    GridLayout gridLayout;

    private static final String TAG_T = "timetableactivity";
    List<String> week_num_List = new ArrayList<>(), start_time_list = new ArrayList<>(), end_time_list = new ArrayList<>();

    View view;
    Button addBtn;
    Button removeBtn;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(timetable.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        gridLayout = findViewById(R.id.schedule);
        et_course = findViewById(R.id.et_course);
        et_section = findViewById(R.id.et_section);
        addBtn = findViewById(R.id.addCourseBtn);
        removeBtn = findViewById(R.id.removeBtn);
        fillcell();


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input1 = et_course.getText().toString();
                String input2 = et_section.getText().toString();
                DownloadTask2 downloadTask = new DownloadTask2(input1, input2);
                downloadTask.execute();

                try {
                    // 1초 동안 스레드 일시 정지
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(timetable.this, timetable.class);
                startActivity(intent);
                fillcell();

            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                removeDataToFile1();
                Intent intent = new Intent(timetable.this, timetable.class);
                startActivity(intent);

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

    private void fillcell() {
        List<String> tableinfo = readDataFromFile1();
        int i = 0;
        while (true) {
            if (i >= tableinfo.size()) {
                break; // tableinfo의 인덱스가 존재하지 않을 때 while 루프를 탈출
            }

            String[] info = tableinfo.get(i).split(",");
            Log.v(TAG_T, "서버 " + info[5]);
            Log.v(TAG_T, "서버 " + info[6]);
            int week_num = week_number(info[2]);
            Log.v(TAG_T, "서버 " + info[5]);
            int sindex = index_num(info[3], info[4]);
            Log.v(TAG_T, "서버 " + info[6]);
            int eindex = index_num(info[5], info[6])-1;
            Log.v(TAG_T, "서버 fillcell" + week_num + sindex + eindex);
            fill(week_num, sindex, eindex);
            i++;
        }
    }

    private void fill(int week_num, int sindex, int eindex) {
        int startRow = sindex; // 시작 행 인덱스 (2번째 줄)
        int endRow = eindex; // 종료 행 인덱스 (5번째 줄)
        int column = week_num; // 병합할 열 인덱스 (2번째 열)

        GridLayout.Spec rowSpec = GridLayout.spec(startRow, endRow - startRow + 1); // 병합할 행 범위
        GridLayout.Spec columnSpec = GridLayout.spec(column); // 병합할 열 범위

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
        layoutParams.setGravity(Gravity.FILL); // 병합된 셀을 채우기 위해 Gravity.FILL 설정
        gridLayout.invalidate();

        // 병합할 셀들을 리스트로 저장
        List<View> viewsToMerge = new ArrayList<>();
        for (int row = startRow; row <= endRow; row++) {
            View view = gridLayout.getChildAt(row * gridLayout.getColumnCount() + column);
            Log.v(TAG_T, "서버: "+view.toString());
            viewsToMerge.add(view);
        }

        // 첫 번째 셀을 병합된 셀로 설정
        View firstView = viewsToMerge.get(0);
        firstView.setBackgroundResource(R.drawable.fill_cell);
        firstView.setLayoutParams(layoutParams);

        // 나머지 셀들에도 병합된 셀의 레이아웃 설정 적용
        for (int i = 1; i < viewsToMerge.size(); i++) {
            View view = viewsToMerge.get(i);
            view.setBackgroundResource(R.drawable.fill_cell);
            view.setLayoutParams(layoutParams);
        }
    }



    private int index_num(String h, String m) {
        int idx = 0;

        if (m != null && m.equals("30")) {
            idx = idx + 1;
        }

        try {
            int hour = Integer.parseInt(h);
            idx = idx + (hour - 8) * 2;
        } catch (NumberFormatException e) {

        }
        if (idx <= 0) {
            idx = 1;
        }

        if (idx >= 19) {
            idx = 20;
        }

        return idx-1;
    }


    private int week_number(String week_str) {
        int week_num;
        if (week_str.equals("mon")) {
            week_num = 0;
        } else if (week_str.equals("tue")) {
            week_num = 1;
        } else if (week_str.equals("wed")) {
            week_num = 2;
        } else if (week_str.equals("thu")) {
            week_num = 3;
        } else {
            week_num = 4;
        }
        return week_num;
    }

    private void writeDataToFile1(String data) {
        try {
            FileOutputStream fileOutputStream = openFileOutput("FILE_NAME", Context.MODE_APPEND);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeDataToFile1() {
        String data = "";
        try {
            FileOutputStream fileOutputStream = openFileOutput("FILE_NAME", Context.MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class DownloadTask2 extends AsyncTask<Void, Void, String> {
        private String course_num;
        private String class_num;

        public DownloadTask2(String course_num, String class_num) {
            this.course_num = course_num;
            this.class_num = class_num;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            try {

                Log.v(TAG_T,"서버 통신 시작");
                String urlString = "http://10.0.2.2/timetable.php?course_name=" + course_num + "&class_num=" + class_num;
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
                    Log.v(TAG_T, "서버 "+ result );
                } else {
                    Log.e(TAG_T, "서버 연결 실패, 응답 코드: " + responseCode);
                }

                // 연결 종료
                connection.disconnect();

            } catch (IOException e) {
                Log.e(TAG_T, "IOException 발생: " + e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Log.d(TAG_T, "서버에서 받은 데이터: " + result);
                displayResult(result);
            } else {
                Log.e(TAG_T, "서버에서 데이터를 읽어오지 못했습니다.");
            }
        }

        private void displayResult(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                week_num_List.clear();
                start_time_list.clear();
                end_time_list.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String week_num = jsonObject.getString("week_num");
                    String start_time = jsonObject.getString("start_class");
                    String end_time = jsonObject.getString("end_class");
                    week_num_List.add(week_num);
                    start_time_list.add(start_time);
                    end_time_list.add(end_time);
                }
                for (int i = 0; i < 2; i++) {
                    String[] start = start_time_list.get(i).split(":");
                    String[] end = end_time_list.get(i).split(":");
                    String week = week_num_List.get(i);
                    String string_info = course_num + "," + class_num + "," + week + ","+ start[0] + "," + start[1] + "," +end[0] + "," + end[1] + "\n";
                    writeDataToFile1(string_info);
                }

            } catch (JSONException e) {
                Log.e(TAG_T, "서버 JSON 파싱 에러: " + e.getMessage());
            }
        }
    }
}