package com.ideas.micro.jasonapp102;

import org.apache.commons.text.RandomStringGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
import java.nio.charset.StandardCharsets;

public class Utility_AES {
    // 用來加密 AES-128-CBC 的 key and iv
    // 會傳給前端的 index.jsp 用來加密帳密
    private static final String TAG = "AES";
    public static String aesKey = "";
    public static String aesIv = "";

    public static void SetAESKey (String key){
        try {
            JSONObject keyjson = new JSONObject(key);
            aesKey = keyjson.getString("key");
            aesIv = keyjson.getString("iv");
//            System.out.println(TAG, aesKey + aesIv);
        } catch (JSONException e){

        }
    }

    public static String Encrypt(String sSrc){
        try {
            byte[] raw = aesKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法 模式 補碼"
            IvParameterSpec iv = new IvParameterSpec(aesIv.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes());
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e){
            return "";
        }
//        return new String(Base64.encode(encrypted, Base64.DEFAULT), StandardCharsets.UTF_8);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    // 解密
    public static String Decrypt(String sSrc){
        try {
            byte[] raw = aesKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(aesIv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.decode(sSrc, Base64.DEFAULT);    // Decode by base64 first
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString;
        } catch (Exception e){
            return "";
        }
    }

    // 亂數文字字串 16 字母(小寫)
    public static String GetRandomString(int length) {
        // Generates a 20 code point string, using only the letters a-z
        RandomStringGenerator generator = new RandomStringGenerator
                .Builder().withinRange('a', 'z').build();
        String randomLetters = generator.generate(length);
        return randomLetters;
    }
}
