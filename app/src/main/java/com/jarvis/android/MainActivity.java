package com.jarvis.android;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private LinearLayout chatMessages;
    private EditText input;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);

        // 기존 JARVIS HUD
        android.webkit.WebView webView = new android.webkit.WebView(this);

        android.webkit.WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setBackgroundColor(Color.rgb(5, 8, 13));
        webView.loadUrl("file:///android_asset/jarvis.html");

        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // ===== 하단 채팅 패널 =====
        LinearLayout chatPanel = new LinearLayout(this);
        chatPanel.setOrientation(LinearLayout.VERTICAL);
        chatPanel.setPadding(16, 10, 16, 10);

        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(Color.argb(205, 3, 10, 18));
        panelBg.setStroke(1, Color.argb(170, 0, 210, 255));
        chatPanel.setBackground(panelBg);

        // 대화 내용
        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        chatMessages = new LinearLayout(this);
        chatMessages.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(chatMessages, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        chatPanel.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        // 입력 영역
        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);

        input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("무엇이든 자연스럽게 입력하세요");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.argb(170, 180, 220, 230));
        input.setTextSize(16);

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.argb(150, 5, 18, 28));
        inputBg.setStroke(1, Color.argb(120, 0, 210, 255));
        input.setBackground(inputBg);

        inputRow.addView(input, new LinearLayout.LayoutParams(
                0,
                58,
                1f
        ));

        Button send = new Button(this);
        send.setText("전송");
        send.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(
                95,
                58
        );
        sendParams.leftMargin = 10;

        inputRow.addView(send, sendParams);
        chatPanel.addView(inputRow);

        // 화면 아래 배치
        FrameLayout.LayoutParams chatParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        250
                );

        chatParams.gravity = Gravity.BOTTOM;
        chatParams.leftMargin = 15;
        chatParams.rightMargin = 15;
        chatParams.bottomMargin = 15;

        root.addView(chatPanel, chatParams);

        send.setOnClickListener(v -> sendMessage());

        input.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });

        setContentView(root);
    }

    private void sendMessage() {
        String text = input.getText().toString().trim();

        if (text.isEmpty()) {
            return;
        }

        addMessage("너", text);
        input.setText("");

        addMessage("JARVIS", "처리 중...");

        JarvisBrain.ask(text, result -> runOnUiThread(() -> {
            // 마지막 "처리 중..." 제거
            if (chatMessages.getChildCount() > 0) {
                chatMessages.removeViewAt(chatMessages.getChildCount() - 1);
            }

            addMessage("JARVIS", result);
        }));
    }

    private void addMessage(String speaker, String message) {
        TextView textView = new TextView(this);

        textView.setText(speaker + "\n" + message);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(15);
        textView.setPadding(12, 8, 12, 8);

        chatMessages.addView(textView);

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
}
