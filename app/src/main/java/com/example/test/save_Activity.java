package com.example.test;



import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;

public class save_Activity extends AppCompatActivity {

    Button save_button1,save_button2,save_button3;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16; // 128 bits
    public static String encrypt(byte[] data, String Key) throws Exception {
        // 生成随机IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        byte[] key = Key.getBytes();
        // 创建密钥规格
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 初始化加密器
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        // 执行加密
        byte[] encrypted = cipher.doFinal(data);

        // 合并IV和加密数据
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_save);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sp=getSharedPreferences("yh", Context.MODE_PRIVATE);

        save_button1=findViewById(R.id.save_button1);
        save_button2=findViewById(R.id.save_button2);
        save_button3=findViewById(R.id.save_button3);
        save_button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor=sp.edit();
                String str;
                try {
                    str = encrypt("yh".getBytes(),"1234567890123456");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                editor.putString("name",str);
                editor.putInt("age",20);
                editor.commit();

            }
        });
        save_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=sp.getString("name"," ")
                        +sp.getInt("age",20);
                Toast toast=Toast.makeText(getApplicationContext(),s, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        save_button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
}