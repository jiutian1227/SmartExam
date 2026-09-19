package com.soft231.smartexam.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soft231.smartexam.config.OllamaConfig;
import com.soft231.smartexam.service.SolutionService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

//题解服务实现类
//调用Ollama AI生成题解 generateSolution
@Service
public class SolutionServiceImpl implements SolutionService {

    @Autowired
    private OllamaConfig ollamaConfig;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> generateSolution(String questionContent, String options,
                                                 String correctAnswer, Integer type,
                                                 String studentAnswer, String existingAnalysis) {
        Map<String, Object> result = new HashMap<>();

        //如果数据库中已有解析，直接返回
        if (existingAnalysis != null && !existingAnalysis.trim().isEmpty()) {
            result.put("solution", existingAnalysis);
            result.put("source", "database");
            return result;
        }

        try {
            String prompt = buildSolutionPrompt(questionContent, options, correctAnswer, type, studentAnswer);
            String solution = callOllama(prompt);
            result.put("solution", solution);
            result.put("source", "ai");
        } catch (Exception e) {
            result.put("solution", "题解生成失败：" + e.getMessage());
            result.put("source", "error");
        }

        return result;
    }

    private String buildSolutionPrompt(String questionContent, String options,
                                        String correctAnswer, Integer type,
                                        String studentAnswer) {
        String typeName = getTypeName(type);
        String optionsInfo = "";
        if (options != null && !options.isEmpty() && (type == 0 || type == 1)) {
            try {
                List<String> optList = objectMapper.readValue(options, List.class);
                StringBuilder sb = new StringBuilder("\n选项：\n");
                for (int i = 0; i < optList.size(); i++) {
                    sb.append(String.format("  %s. %s\n", (char) ('A' + i), optList.get(i)));
                }
                optionsInfo = sb.toString();
            } catch (Exception e) {
                optionsInfo = "\n选项：" + options;
            }
        }

        return String.format("""
            你是一位专业的学科教师，请为以下题目生成详细的题解（题目解析）。

            题目类型：%s
            题目内容：%s%s
            正确答案：%s
            学生答案：%s

            请按照以下结构生成题解：

            ## 考查知识点
            简要说明本题考查的知识点。

            ## 解题思路
            详细分析解题的思考过程和方法。

            ## 详细解析
            逐步解析题目，说明为什么正确答案是正确的，错误选项为什么错。

            ## 易错点提示
            指出学生容易出错的地方和注意事项。

            ## 学习建议
            针对该知识点给出学习建议。

            ## 注意
            - 直接输出题解内容，不要添加额外的开头语或结尾语
            - 用中文回答
            - 题解内容要详细、准确、有针对性
            - 适当使用换行和分段让内容清晰易读
            """, typeName, questionContent, optionsInfo, correctAnswer, studentAnswer != null ? studentAnswer : "未作答");
    }

    private String getTypeName(int type) {
        switch (type) {
            case 0: return "单选题";
            case 1: return "多选题";
            case 2: return "判断题";
            case 3: return "填空题";
            case 4: return "简答题";
            default: return "未知题型";
        }
    }

    private String callOllama(String userPrompt) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaConfig.getModel());

        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", """
            你是一位经验丰富的学科教师，擅长为学生提供详细的题目解析和学习指导。
            你的题解应该：
            1. 准确分析题目考查的知识点
            2. 提供清晰的解题思路和步骤
            3. 指出易错点和学习建议
            4. 用鼓励性的语言帮助学生理解
            5. 内容详细但不啰嗦，层次分明

            注意：这是全新的对话，不要参考之前的任何对话内容。
            """);
        messages.add(systemMessage);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.7);
        options.put("num_predict", 2048);
        requestBody.put("options", options);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(ollamaConfig.getUrl() + "/api/chat")
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json; charset=UTF-8")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=UTF-8")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama API调用失败: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Ollama API返回空响应");
            }

            String responseBody = body.string();
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);
            String content = jsonNode.get("message").get("content").asText();
            return cleanContent(content);
        }
    }

    private String cleanContent(String content) {
        if (content == null) return "";
        //解码Unicode转义
        content = decodeUnicode(content);
        //清理多余的空行
        content = content.replaceAll("\\n{3,}", "\n\n");
        return content.trim();
    }

    private String decodeUnicode(String content) {
        if (content == null) return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < content.length()) {
            char c = content.charAt(i);
            if (c == '\\' && i + 1 < content.length() && content.charAt(i + 1) == 'u') {
                if (i + 5 < content.length()) {
                    try {
                        String unicode = content.substring(i + 2, i + 6);
                        char decoded = (char) Integer.parseInt(unicode, 16);
                        sb.append(decoded);
                        i += 6;
                        continue;
                    } catch (NumberFormatException e) {
                        //忽略解析错误
                    }
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }
}
