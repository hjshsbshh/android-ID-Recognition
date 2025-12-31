package com.example.test.Utils;

import org.bouncycastle.jcajce.io.CipherOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.security.Security;

public class SM4Util {
    private static final String ALGORITHM_NAME = "SM4";
    private static final String TRANSFORMATION = "SM4/ECB/PKCS5Padding";

    /**
     * 加密文件
     */
    public static void encryptFile(byte[] key, File inputFile, File outputFile) throws Exception {
        processFile(key, inputFile, outputFile, Cipher.ENCRYPT_MODE);
    }

    /**
     * 解密文件
     */
    public static void decryptFile(byte[] key, File inputFile, File outputFile) throws Exception {
        processFile(key, inputFile, outputFile, Cipher.DECRYPT_MODE);
    }

    private static void processFile(byte[] key, File inputFile, File outputFile, int mode) throws Exception {
        // 添加Bouncy Castle提供者
        Security.addProvider(new BouncyCastleProvider());

        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_NAME);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
        cipher.init(mode, keySpec);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 生成随机密钥
     */
    public static byte[] generateKey() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return key;
    }
}