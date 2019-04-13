package com.farm.innovation.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author wbs on 11/25/17.
 */

public class JsonHelper {
    public static int getInt(JSONObject jo, String key) {
        int value = 0;
        try {
            value = jo.getInt(key);
        } catch (Exception e) {
            ;
        }
        return value;
    }

    public static String getString(JSONObject jo, String key) {
        String value = "";
        try {
            value = jo.getString(key);
        } catch (Exception e) {
            ;
        }
        return value;
    }

    public static void putString(JSONObject jo, String key, String value) {
        try {
            jo.put(key, value);
        } catch (JSONException e) {
            ;
        }
    }

    public static void putInt(JSONObject jo, String key, int value) {
        try {
            jo.put(key, value);
        } catch (JSONException e) {
            ;
        }
    }

    public static JSONObject getJsonObj(JSONObject jo, String key) {
        JSONObject value = null;
        try {
            value = jo.optJSONObject(key);
        } catch (Exception e) {
            ;
        }
        return value;
    }

    public static void putJsonObject(JSONObject jo, String key, JSONObject value) {
        try {
            jo.put(key, value);
        } catch (Exception e) {
            ;
        }
    }


    /**
     * 将map转为json
     *
     * @param map
     * @param sb
     * @return
     */
    public static StringBuilder mapToJson(Map<?, ?> map, StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        sb.append("{");
        Iterator<?> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
            String key = entry.getKey() != null ? entry.getKey().toString() : "";
            sb.append("\"" + stringToJson(key) + "\":");
            Object o = entry.getValue();
            if (o instanceof List<?>) {
                List<?> l = (List<?>) o;
                listToJson(l, sb);
            } else if (o instanceof Map<?, ?>) {
                Map<?, ?> m = (Map<?, ?>) o;
                mapToJson(m, sb);
            } else {
                String val = entry.getValue() != null ? entry.getValue().toString() : "";
                sb.append("\"" + stringToJson(val) + "\"");
            }
            if (iter.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb;
    }

    public static StringBuilder listToJson(List<?> lists, StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        sb.append("[");
        for (int i = 0; i < lists.size(); i++) {
            Object o = lists.get(i);
            if (o instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) o;
                mapToJson(map, sb);
            } else if (o instanceof List<?>) {
                List<?> l = (List<?>) o;
                listToJson(l, sb);
            } else {
                sb.append("\"" + o + "\"");
            }
            if (i != lists.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb;
    }


    /**
     * 将字符串转为json数据
     *
     * @param str 数据字符串
     * @return json字符串
     */
    private static String stringToJson(String str) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
