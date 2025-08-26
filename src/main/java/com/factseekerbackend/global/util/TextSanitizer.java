package com.factseekerbackend.global.util;

import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

public final class TextSanitizer {

    private TextSanitizer() {}

    public static String sanitizeTitle(String raw) {
        if (raw == null) return "";
        // 1) Decode HTML entities (e.g., &quot; &#39; &amp;)
        String unescaped = HtmlUtils.htmlUnescape(raw);
        // 1.5) Remove JSON-style escape for quotes/backslashes (e.g., \" -> ")
        String jsonLikeUnescaped = unescaped.replaceAll("\\\\([\"'\\/])", "$1");
        // 2) Strip simple HTML tags if any slipped through
        String noTags = jsonLikeUnescaped.replaceAll("<[^>]+>", "");
        // 3) Collapse whitespace and trim
        String collapsed = noTags.replaceAll("\\s+", " ").trim();
        // 4) Remove any remaining standalone backslashes (defensive)
        String noBackslashes = collapsed.replace("\\", "");
        return StringUtils.hasText(noBackslashes) ? noBackslashes : "";
    }
}
