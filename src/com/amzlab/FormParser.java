package com.amzlab;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FormParser {
    /**
     * 处理 POST multipart/form-data 表单数据 <a href="https://zhuanlan.zhihu.com/p/195726295">解析原理</a>
     *
     * @param data      原始报文
     * @param splitLine 报文 boundary 分隔符
     * @return 键值对
     */
    public static Map<String, String> parseFormData(String data, String splitLine) {
        Map<String, String> result = new HashMap<>();

        Scanner sc = new Scanner(data);
        String line = sc.nextLine();
        String key = null;
        StringBuilder value = new StringBuilder();
        while (sc.hasNextLine()) {
            // 键值对凑齐了就添加
            if (key != null && value.length() != 0) {
                result.put(key, value.toString());
                key = null;
                value.delete(0, value.length());
            }
            // 一次判断两行
            if (line.contains(splitLine) && (line = sc.nextLine()).startsWith("Content-Disposition")) {
                key = line.split("; ")[1].split("=")[1].replaceAll("\"", "");
                while (sc.hasNextLine()) {
                    line = sc.nextLine();
                    if (line.isEmpty()) continue; // 跳过空行
                    if (line.contains(splitLine)) break; // 末行尾部有 "--" 所以用 startsWith 匹配
                    value.append(line);
                }
            }
        }
        // 最后一个键值对凑齐了但会跳出循环, 所以就在外部添加
        if (key != null && value.length() != 0) {
            result.put(key, value.toString());
        }

        return result;
    }
}
