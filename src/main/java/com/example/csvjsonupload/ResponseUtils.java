package com.example.csvjsonupload;

import org.yaml.snakeyaml.scanner.Constant;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtils {
    public static Map<String, Object> response(Map<String, Object> response, Object dataMap) {
        Map<String, Object> result = new HashMap<>();

        result.put("code", "0000");
        response.put("result", result);
        response.put("data", dataMap);

        return response;
    }
}
