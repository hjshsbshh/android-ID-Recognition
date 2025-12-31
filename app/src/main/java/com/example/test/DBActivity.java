package com.example.test;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DBActivity extends AppCompatActivity {

    private MyDbHelper mydbhelper;
    private SQLiteDatabase database;
    Button button12, button13, button14, button15;
    EditText editText1, editText2;
    TextView textViewResult; // 用于显示查询结果

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dbactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mydbhelper = new MyDbHelper(this, "yh", null, 1);
        database = mydbhelper.getWritableDatabase(); // 改为可写数据库
        database.execSQL("CREATE TABLE IF NOT EXISTS person (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "age INTEGER" +
                ");");

//        String sql = "delete from person where _id != 1 ";//删除操作的SQL语句
//        database.execSQL(sql);//执行删除操作

        button12 = findViewById(R.id.button12);
        button13 = findViewById(R.id.button13);
        button14 = findViewById(R.id.button14);
        button15 = findViewById(R.id.button15);
        editText1 = findViewById(R.id.editTextText);
        editText1.setHint("input name");
        editText2 = findViewById(R.id.editTextText2);
        editText2.setHint("input age");
        textViewResult = findViewById(R.id.textViewResult);

        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
            }
        });

        button13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData();
            }
        });

        button14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });

        button15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryData();
            }
        });
    }

    private void insertData() {
        try {
            String name = editText1.getText().toString().trim();
            String ageStr = editText2.getText().toString().trim();

            if (name.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(this, "姓名和年龄不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            int age = Integer.parseInt(ageStr);
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            cv.put("age", age);

            long result = database.insert("person", null, cv);

            if (result != -1) {
                Toast.makeText(this, "插入成功，ID: " + result, Toast.LENGTH_SHORT).show();
                clearInputs();
            } else {
                Toast.makeText(this, "插入失败", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "年龄必须是数字", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DB", "插入数据异常", e);
            Toast.makeText(this, "插入数据时出错", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteData() {
        try {
            String name = editText1.getText().toString().trim();
            String ageStr = editText2.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            int deletedRows;
            if (!ageStr.isEmpty()) {
                int age = Integer.parseInt(ageStr);
                // 根据姓名和年龄删除
                deletedRows = database.delete("person", "name=? AND age=?", new String[]{name, String.valueOf(age)});
            } else {
                // 只根据姓名删除
                deletedRows = database.delete("person", "name=?", new String[]{name});
            }

            if (deletedRows > 0) {
                Toast.makeText(this, "删除成功，删除了 " + deletedRows + " 条记录", Toast.LENGTH_SHORT).show();
                clearInputs();
            } else {
                Toast.makeText(this, "没有找到匹配的记录", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "年龄必须是数字", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DB", "删除数据异常", e);
            Toast.makeText(this, "删除数据时出错", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateData() {
        try {
            String name = editText1.getText().toString().trim();
            String ageStr = editText2.getText().toString().trim();

            if (name.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(this, "姓名和年龄都不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            int newAge = Integer.parseInt(ageStr);

            ContentValues values = new ContentValues();
            values.put("age", newAge);

            int updatedRows = database.update("person", values, "name=?", new String[]{name});

            if (updatedRows > 0) {
                Toast.makeText(this, "更新成功，更新了 " + updatedRows + " 条记录", Toast.LENGTH_SHORT).show();
                clearInputs();
            } else {
                Toast.makeText(this, "没有找到姓名为 " + name + " 的记录", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "年龄必须是数字", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DB", "更新数据异常", e);
            Toast.makeText(this, "更新数据时出错", Toast.LENGTH_SHORT).show();
        }
    }

    private void queryData() {
        try {
            String name = editText1.getText().toString().trim();
            String ageStr = editText2.getText().toString().trim();

            clearInputs();
            Cursor cursor;
            if (!name.isEmpty() && !ageStr.isEmpty()) {
                // 根据姓名和年龄精确查询
                int age = Integer.parseInt(ageStr);
                cursor = database.query("person", null, "name=? AND age=?",
                        new String[]{name, String.valueOf(age)}, null, null, null);
            } else if (!name.isEmpty()) {
                // 根据姓名查询
                cursor = database.query("person", null, "name=?",
                        new String[]{name}, null, null, null);
            } else if (!ageStr.isEmpty()) {
                // 根据年龄查询
                int age = Integer.parseInt(ageStr);
                cursor = database.query("person", null, "age=?",
                        new String[]{String.valueOf(age)}, null, null, null);
            } else {
                // 查询所有数据
                cursor = database.query("person", null, null, null, null, null, null);
            }

            // 显示查询结果
            displayQueryResult(cursor);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "年龄必须是数字", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DB", "查询数据异常", e);
            Toast.makeText(this, "查询数据时出错", Toast.LENGTH_SHORT).show();
        }
    }

    // 显示查询结果
    private void displayQueryResult(Cursor cursor) {
        StringBuilder result = new StringBuilder();
        result.append("查询结果：\n");

        if (cursor != null && cursor.moveToFirst()) {
            int count = 0;
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));

                result.append("ID: ").append(id)
                        .append(", 姓名: ").append(name)
                        .append(", 年龄: ").append(age)
                        .append("\n");
                count++;
            } while (cursor.moveToNext());

            result.append("\n共找到 ").append(count).append(" 条记录");
            cursor.close();
        } else {
            result.append("没有找到匹配的记录");
            if (cursor != null) {
                cursor.close();
            }
        }

        // 显示结果
        if (textViewResult != null) {
            textViewResult.setText(result.toString());
        }
    }

    // 清空输入框
    private void clearInputs() {
        editText1.setText("");
        editText2.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (mydbhelper != null) {
            mydbhelper.close();
        }
    }
}