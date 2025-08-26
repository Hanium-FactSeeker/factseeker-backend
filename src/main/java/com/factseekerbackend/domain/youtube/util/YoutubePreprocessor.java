package com.factseekerbackend.domain.youtube.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YoutubePreprocessor {

    private YoutubePreprocessor() {}

    // Basic HTML entity decoding for common cases seen in YouTube titles
    public static String decodeBasicHtmlEntities(String s) {
        if (s == null) return null;
        String out = s
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ");

        // Numeric decimal entities: &#34; → '"'
        {
            Pattern dec = Pattern.compile("&#(\\d+);");
            Matcher mm = dec.matcher(out);
            out = mm.replaceAll(r -> {
                try {
                    int code = Integer.parseInt(r.group(1));
                    return new String(Character.toChars(code));
                } catch (Exception e) {
                    return r.group(0);
                }
            });
        }

        // Numeric hex entities: &#x27; → '\''
        {
            Pattern hex = Pattern.compile("&#x([0-9A-Fa-f]+);");
            Matcher mm = hex.matcher(out);
            out = mm.replaceAll(r -> {
                try {
                    int code = Integer.parseInt(r.group(1), 16);
                    return new String(Character.toChars(code));
                } catch (Exception e) {
                    return r.group(0);
                }
            });
        }
        return out;
    }

    // Remove control characters and normalize whitespace to single spaces
    public static String normalizeWhitespace(String s) {
        if (s == null) return null;
        // Strip control chars except tab/newline, then collapse whitespace
        String cleaned = s.replaceAll("[\\p{Cntrl}&&[^[\t\n]]]", "");
        cleaned = cleaned.replace('\n', ' ').replace('\r', ' ');
        cleaned = cleaned.replaceAll("\\s+", " ");
        return cleaned.trim();
    }

    // Sanitize any display text (titles, channel names)
    public static String sanitizeDisplayText(String s) {
        if (s == null) return "";
        String decoded = decodeBasicHtmlEntities(s);
        return normalizeWhitespace(decoded);
    }

    // Limit query length and normalize whitespace to avoid API errors
    public static String sanitizeSearchQuery(String query) {
        if (query == null) return "";
        String q = normalizeWhitespace(query);
        if (q.length() > 200) {
            q = q.substring(0, 200);
        }
        return q;
    }

    // Extract a YouTube video ID from a variety of URL formats or return the input if it's already an ID.
    public static String extractVideoId(String input) {
        if (input == null || input.isBlank()) return null;
        String s = input.trim();
        // If looks like a plain ID (not a URL), return as-is
        if (!s.startsWith("http")) return s;

        try {
            // Standard watch URL
            int vIdx = s.indexOf("v=");
            if (vIdx != -1) {
                String candidate = s.substring(vIdx + 2);
                int amp = candidate.indexOf('&');
                if (amp != -1) candidate = candidate.substring(0, amp);
                return candidate;
            }

            // youtu.be short URL
            String youtu = "youtu.be/";
            int shortIdx = s.indexOf(youtu);
            if (shortIdx != -1) {
                String candidate = s.substring(shortIdx + youtu.length());
                int q = candidate.indexOf('?');
                if (q != -1) candidate = candidate.substring(0, q);
                return candidate;
            }

            // Shorts URL
            String shorts = "/shorts/";
            int shortsIdx = s.indexOf(shorts);
            if (shortsIdx != -1) {
                String candidate = s.substring(shortsIdx + shorts.length());
                int q = candidate.indexOf('?');
                if (q != -1) candidate = candidate.substring(0, q);
                return candidate;
            }

            // Last fallback: try to find 11+ char video id-like token
            Pattern p = Pattern.compile("[a-zA-Z0-9_-]{11,}");
            Matcher m = p.matcher(s);
            if (m.find()) {
                return m.group();
            }
        } catch (Exception ignored) {
        }
        return s; // fallback to original
    }
}
