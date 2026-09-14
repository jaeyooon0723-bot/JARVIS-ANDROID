package com.jarvis.android;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JarvisBrain {

    private static final String SERVER_URL =
            "http://127.0.0.1:8080/v1/chat/completions";

    private static final JSONArray history = new JSONArray();

    public interface Callback {
        void onResult(String result);
    }

    public static synchronized void ask(String prompt, Callback callback) {
        new Thread(() -> {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URL(SERVER_URL).openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(120000);

                if (history.length() == 0) {
                    JSONObject system = new JSONObject();
                    system.put("role", "system");
                    system.put("content",
                            "너는 JARVIS AI Brain이다. " +
                            "사용자의 자연어 의도를 이해하고 문맥에 맞게 답한다. " +
                            "정해진 명령어 목록이나 키워드에 의존하지 않는다. " +
                            "한국어를 기본으로 사용한다. " +
                            "답변은 핵심만 간결하게 한다.");
                    history.put(system);
                }

                JSONObject user = new JSONObject();
                user.put("role", "user");
                user.put("content", prompt);
                history.put(user);

                while (history.length() > 11) {
                    history.remove(1);
                }

                JSONObject body = new JSONObject();
                body.put("model", "Qwen3-4B-Q4_K_M.gguf");
                body.put("messages", history);
                body.put("temperature", 0.7);
                body.put("max_tokens", 128);

                JSONObject kwargs = new JSONObject();
                kwargs.put("enable_thinking", false);
                body.put("chat_template_kwargs", kwargs);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("UTF-8"));
                }

                int code = conn.getResponseCode();

                InputStream input = code >= 400
                        ? conn.getErrorStream()
                        : conn.getInputStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(input, "UTF-8"));

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                conn.disconnect();

                JSONObject json = new JSONObject(response.toString());

                if (!json.has("choices")) {
                    callback.onResult("응답을 받지 못했습니다.");
                    return;
                }

                String answer = json
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .optString("content", "");

                JSONObject assistant = new JSONObject();
                assistant.put("role", "assistant");
                assistant.put("content", answer);
                history.put(assistant);

                callback.onResult(answer.trim());

            } catch (Exception e) {
                callback.onResult("Brain 오류: " + e.getMessage());
            }
        }).start();
    }

    public static synchronized void clearHistory() {
        while (history.length() > 0) {
            history.remove(history.length() - 1);
        }
    }
}
