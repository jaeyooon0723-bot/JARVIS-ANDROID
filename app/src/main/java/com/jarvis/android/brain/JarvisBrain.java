package com.jarvis.android.brain;

public class JarvisBrain {

    public static String think(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "입력이 없습니다.";
        }

        String text = input.trim().toLowerCase();

        if (text.contains("안녕")) {
            return "안녕하세요. JARVIS 시스템이 준비되었습니다.";
        }

        if (text.contains("상태")) {
            return "시스템 정상. 현재 HUD 연결을 확인했습니다.";
        }

        if (text.contains("시간")) {
            return "현재 시간 기능은 다음 단계에서 연결합니다.";
        }

        return "명령을 분석했습니다: " + input;
    }
}
