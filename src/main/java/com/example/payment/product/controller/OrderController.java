package com.example.payment.product.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin("*")
// 일단 모든 요청을 다 허용한다.
// 나중에 프론트가 개발되면 특정 주소만 허용해주는 것으로 수정해야 함.
@RequestMapping("/order")
public class OrderController {

    /**
     * hello
     * @return
     * @throws IOException
     */
    public String getToken() throws IOException {
        HttpsURLConnection conn = null;

        URL url = new URL("https://api.iamport.kr/users/getToken");
        conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        JsonObject json = new JsonObject();
        json.addProperty("imp_key","1861881235228725");
        json.addProperty("imp_secret","z74Jn4UxTEJZulLFebu6jpgEH1ag6MPLst9lSfhCBUrSMQdUKPJpMIFQxTh0nUrwHQ068s08JUuSgPda");

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        bw.write(json.toString());
        bw.flush();
        bw.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
        Gson gson = new Gson();
        String response = gson.fromJson(br.readLine(), Map.class).get("response").toString();
        System.out.println(response);
        String token = gson.fromJson(response, Map.class).get("access_token").toString();
        br.close();
        conn.disconnect();
        return token;
    }
    @RequestMapping(method = RequestMethod.GET, value = "/get/payInfo")
    public Map<String, String> getPaymentInfo(String impUid) throws IOException {
        String token = getToken();
        HttpsURLConnection conn = null;
        // http method, header 설정
        URL url = new URL("https://api.iamport.kr/payments/" + impUid);
        // + ""에 imp_uid가 들어가야 함.
        conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", token);
        conn.setDoOutput(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        Gson gson = new Gson();
        String response = gson.fromJson(br.readLine(), Map.class).get("response").toString();
        br.close();
        conn.disconnect();
        String amount = response.split("amount")[1].split(",")[0].replace("=", "");
        String name = response.split("name")[1].split(",")[0].replace("=", "");
        Map<String, String> result = new HashMap<>();
        result.put("amount", amount);
        result.put("name", name);
        return result;
    }




    // 결제 취소(환불) 메소드
    public ResponseEntity payMentCancel(String access_token, String imp_uid, String amount, String reason) throws IOException {
        System.out.println("imp_uid = " + imp_uid);
        HttpsURLConnection conn = null;
        URL url = new URL("https://api.iamport.kr/payments/cancel");
        conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", access_token);
        conn.setDoOutput(true);
        JsonObject json = new JsonObject();
        json.addProperty("reason", reason);
        json.addProperty("imp_uid", imp_uid);
        json.addProperty("amount", amount);
        json.addProperty("checksum", amount);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        bw.write(json.toString());
        bw.flush();
        bw.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        br.close();
        conn.disconnect();
        return ResponseEntity.ok().body("payment cancel success");
    }
    // 실질적으로 결제를 검증하는 메소드
    @RequestMapping("/validation")
    public ResponseEntity paymentValidation(String impUid) throws IOException {
        String dbPrice = "60000.0";     // 원래는 repository에서 findByName으로 db에서 조회해서 가져와야 하는 값.
        Map<String, String> paymentResult = getPaymentInfo(impUid);
        if (paymentResult.get("amount").equals(dbPrice)) {
            return ResponseEntity.ok().body("ok");
        } else {
            // 환불처리
            String token = getToken();
            payMentCancel(token, impUid, paymentResult.get("amount"), "결제 금액 에러");
            return ResponseEntity.badRequest().body("error");
        }
    }



}