package virtual_robot.lessons;

import java.util.*;

public class JsonParser {
    private final String src;
    private int pos;

    private JsonParser(String src) {
        this.src = src;
        this.pos = 0;
    }

    public static Object parse(String json) {
        return new JsonParser(json.trim()).parseValue();
    }

    private Object parseValue() {
        skipWs();
        char c = peek();
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (src.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
        if (src.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        if (src.startsWith("null", pos)) { pos += 4; return null; }
        return parseNumber();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseObject() {
        consume('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWs();
        if (peek() == '}') { pos++; return map; }
        while (true) {
            String key = parseString();
            skipWs(); consume(':');
            map.put(key, parseValue());
            skipWs();
            if (peek() == '}') { pos++; break; }
            consume(',');
        }
        return map;
    }

    private List<Object> parseArray() {
        consume('[');
        List<Object> list = new ArrayList<>();
        skipWs();
        if (peek() == ']') { pos++; return list; }
        while (true) {
            list.add(parseValue());
            skipWs();
            if (peek() == ']') { pos++; break; }
            consume(',');
        }
        return list;
    }

    private String parseString() {
        skipWs(); consume('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') break;
            if (c == '\\') {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private double parseNumber() {
        int start = pos;
        while (pos < src.length() && "-0123456789.eE+".indexOf(src.charAt(pos)) >= 0) pos++;
        return Double.parseDouble(src.substring(start, pos));
    }

    private void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    private char peek() { skipWs(); return src.charAt(pos); }

    private void consume(char c) {
        skipWs();
        if (pos >= src.length() || src.charAt(pos) != c)
            throw new RuntimeException("Expected '" + c + "' at pos " + pos
                    + " got '" + (pos < src.length() ? src.charAt(pos) : "EOF") + "'");
        pos++;
    }
}
