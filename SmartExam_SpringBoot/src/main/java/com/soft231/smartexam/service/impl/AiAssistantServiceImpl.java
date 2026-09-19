package com.soft231.smartexam.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soft231.smartexam.config.OllamaConfig;
import com.soft231.smartexam.service.AiAssistantService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//AI助手服务实现类
//问答：回答学生问题 answerQuestion
//流式输出：流式回答学生问题 answerQuestionStream
@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    @Autowired
    private OllamaConfig ollamaConfig;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    //回答学生问题
    @Override
    public Map<String, Object> answerQuestion(String question, String subject) throws Exception {
        String prompt = buildPrompt(question, subject);
        String response = callOllama(prompt);

        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("answer", response);
        result.put("success", true);
        return result;
    }

    private String buildPrompt(String question, String subject) {
        String subjectInfo = subject != null && !subject.isEmpty() 
            ? "当前问题涉及的学科是：" + subject + "。" 
            : "";

        return String.format("""
            %s
            
            学生问题：%s
            
            请根据以上规则回答学生的问题。
            """, subjectInfo, question);
    }

    private String callOllama(String userPrompt) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaConfig.getModel());

        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", """
            你是一个专注于解答学生学术问题的AI助手，属于SmartExam系统。
            
            ## 处理规则：
            1. 如果用户发送的是问候语（如：你好、嗨、您好、hello、hi）才回答这句话："你好！请问有什么学习问题需要帮助吗？"

            2. 如果用户的问题是学习相关的学术问题，直接回答：
               - 学科知识讲解（语文、数学、英语、物理、化学、生物、历史、地理、政治、计算机等）
               - 作业难题解答、考试复习指导、学习方法建议
               - 知识点概念解释、公式推导、阅读理解、编程问题等

            3. 如果用户的问题是非学习类问题（娱乐八卦、游戏攻略、购物推荐、情感咨询、生活琐事等），礼貌拒绝并引导："这个问题不在我的服务范围内，我专注于解答学习相关的学术问题。请提问学科知识问题。"

            4. 如果用户的请求是不当内容（违法违规、作弊请求、攻击性言论、敏感话题等），直接拒绝："抱歉，我无法回答这类问题。"

            ## 回答规范：
            - 回答要准确、清晰、有条理，长度适中（一般不超过500字）
            - 计算题给出详细步骤，概念题给出准确定义
            - 不提供完整作业答案或考试答案（防止作弊），只提供思路和引导
            - 直接回复问题，不需要自我介绍

            ## 注意：
            - 这是全新的对话，不要参考之前的任何对话内容
            - 你的回答只需包含针对用户问题的直接回复
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
        options.put("num_predict", 800);
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
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String content = jsonNode.get("message").get("content").asText();
            return decodeUnicode(content).trim();
        }
    }

    private String cleanMarkdown(String content) {
        if (content == null) {
            return "";
        }
        content = decodeUnicode(content);
        content = content.replaceAll("\\*\\*([^*]+)\\*\\*", "wwwrrrr$1wwwrrrr");
        content = content.replaceAll("\\*([^*]+)\\*", "$1");
        content = content.replaceAll("```[\\s\\S]*?```", "");
        content = content.replaceAll("`([^`]+)`", "$1");
        content = content.replaceAll("^#{1,6}\\s+", "");
        content = content.replaceAll("^[\\-*+]\\s+", "");
        content = content.replaceAll("^\\d+\\.\\s+", "");
        content = content.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1");
        content = content.replaceAll("\\n{3,}", "\n\n");
        content = content.replaceAll("\\*+", "\n");
        content = processLatexSymbols(content);
        return content.trim();
    }

    private String processLatexSymbols(String content) {
        content = content.replaceAll("\\$", "");
        content = content.replaceAll("\\\\neq", "≠");
        content = content.replaceAll("\\\\geq", "≥");
        content = content.replaceAll("\\\\leq", "≤");
        content = content.replaceAll("\\\\times", "×");
        content = content.replaceAll("\\\\div", "÷");
        content = content.replaceAll("\\\\pm", "±");
        content = content.replaceAll("\\\\sqrt", "√");
        content = content.replaceAll("\\\\alpha", "α");
        content = content.replaceAll("\\\\beta", "β");
        content = content.replaceAll("\\\\gamma", "γ");
        content = content.replaceAll("\\\\theta", "θ");
        content = content.replaceAll("\\\\pi", "π");
        content = content.replaceAll("\\\\sum", "∑");
        content = content.replaceAll("\\\\int", "∫");
        content = content.replaceAll("\\\\infty", "∞");
        content = content.replaceAll("\\\\frac\\{([^\\}]+)\\}\\{([^\\}]+)\\}", "$1/$2");
        content = content.replaceAll("\\\\frac\\s*\\{([^\\}]+)\\}\\s*\\{([^\\}]+)\\}", "$1/$2");
        content = content.replaceAll("\\\\+", "");
        return content;
    }

    private String decodeUnicode(String content) {
        if (content == null) {
            return "";
        }
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
                    }
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    //流式回答学生问题
    @Override
    public java.io.InputStream answerQuestionStream(String question, String subject) throws Exception {
        String prompt = buildPrompt(question, subject);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaConfig.getModel());

        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", """
            你是一个专注于解答学生学术问题的AI助手。

            ## 处理规则：
            1. 如果用户的问题是学习相关的学术问题，直接回答：
               - 学科知识讲解（语文、数学、英语、物理、化学、生物、历史、地理、政治、计算机等）
               - 作业难题解答、考试复习指导、学习方法建议
               - 知识点概念解释、公式推导、阅读理解、编程问题等

            2. 如果用户的问题是非学习类问题（娱乐八卦、游戏攻略、购物推荐、情感咨询、生活琐事等），礼貌拒绝并引导："这个问题不在我的服务范围内，我专注于解答学习相关的学术问题。请提问学科知识问题。"

            3. 如果用户的请求是不当内容（违法违规、作弊请求、攻击性言论、敏感话题等），直接拒绝："抱歉，我无法回答这类问题。"
            
            4. 对于不同于以上的情况才回答："你好！请问有什么学习问题需要帮助吗？"

            ## 回答规范：
            - 回答要准确、清晰、有条理，长度适中（一般不超过500字）
            - 计算题给出详细步骤，概念题给出准确定义
            - 不提供完整作业答案或考试答案（防止作弊），只提供思路和引导
            - 直接回复问题，不需要自我介绍

            ## 注意：
            - 这是全新的对话，不要参考之前的任何对话内容
            - 你的回答只需包含针对用户问题的直接回复
            """);
        messages.add(systemMessage);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("stream", true);

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.7);
        options.put("num_predict", 800);
        requestBody.put("options", options);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(ollamaConfig.getUrl() + "/api/chat")
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json; charset=UTF-8")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=UTF-8")))
                .build();

        Response response = httpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Ollama API调用失败: " + response.code());
        }

        return response.body().byteStream();
    }
}