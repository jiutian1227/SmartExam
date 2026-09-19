package com.soft231.smartexam.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soft231.smartexam.config.OllamaConfig;
import com.soft231.smartexam.service.AiQuestionService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//AI题目服务实现类
//出题：AI生成题目 generateQuestions
//批阅：AI批改主观题 gradeSubjectiveQuestion
@Service
public class AiQuestionServiceImpl implements AiQuestionService {

    @Autowired
    private OllamaConfig ollamaConfig;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    //生成题目
    public List<Map<String, Object>> generateQuestions(int type, int count, String requirement, Long knowledgePointId, String knowledgePointName) throws IOException {
        String typeName = getTypeName(type);

        String prompt = buildPrompt(typeName, count, requirement, knowledgePointName);

        String ollamaResponse = callOllama(prompt);

        return parseQuestionsFromResponse(ollamaResponse, type, knowledgePointId);
    }

    private String getTypeName(int type) {
        switch (type) {
            case 0: return "单选题";
            case 1: return "多选题";
            case 2: return "判断题";
            case 3: return "填空题";
            case 4: return "简答题";
            default: return "单选题";
        }
    }

    private String buildPrompt(String typeName, int count, String requirement, String knowledgePointName) {
        String knowledgePointInfo = knowledgePointName != null ? "所属知识点：" + knowledgePointName + "。" : "";
        return String.format("""
            请帮我生成 %d 道%s。%s%s

            要求：
            1. 请以JSON数组格式返回，只需要纯JSON，不要其他文字说明
            2. 每道题包含以下字段：
               - content: 题目内容（字符串）
               - answer: 正确答案（字符串），对于单选题和多选题，答案格式为字母如 "A" 或 "A,C"，不要带选项内容
               - analysis: 题目解析（字符串）
               %s
            3. 直接返回JSON数组，不要有markdown格式或其他说明文字
            4. 选项内容要与题目相关且有迷惑性，选项内容不要只是简单的字母重复
            """,
            count, typeName, knowledgePointInfo, requirement != null ? requirement : "",
            getTypeSpecificFields(typeName));
    }

    private String getTypeSpecificFields(String typeName) {
        switch (typeName) {
            case "单选题":
                return "- options: 选项数组，格式为 [\"选项内容\", \"选项内容\", \"选项内容\", \"选项内容\"]\n" +
                       "         - 选项内容不要包含字母前缀（如A.、B.），直接写内容\n" +
                       "         - answer字段格式：只返回一个字母如 \"A\"";
            case "多选题":
                return "- options: 选项数组，格式为 [\"选项内容\", \"选项内容\", \"选项内容\", \"选项内容\"]\n" +
                       "         - 选项内容不要包含字母前缀（如A.、B.），直接写内容\n" +
                       "         - answer字段格式：返回多个字母用逗号分隔，如 \"A,C\" 或 \"A,B,D\"\n" +
                       "         - 必须选择2个或以上的正确答案";
            case "判断题":
                return "- answer字段只返回 \"正确\" 或 \"错误\"";
            case "填空题":
                return "- answer字段用英文逗号分隔多个答案，不要返回字母";
            case "简答题":
                return "- answer字段：返回详细的文字答案，不要返回字母选项（如A、B、C、D）\n" +
                       "         - 答案要完整、详细，涵盖问题的核心要点";
            default:
                return "";
        }
    }

    private String callOllama(String prompt) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaConfig.getModel());

        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的出题专家，擅长生成各种类型的考试题目。注意：这是全新的对话，不要参考之前的任何对话内容。");
        messages.add(systemMessage);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.7);
        options.put("max_tokens", 1000);
        requestBody.put("options", options);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(ollamaConfig.getUrl() + "/api/chat")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama API call failed: " + response);
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("message").get("content").asText();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseQuestionsFromResponse(String response, int type, Long knowledgePointId) throws IOException {
        List<Map<String, Object>> questions = new ArrayList<>();

        try {
            String jsonStr = extractJsonFromResponse(response);
            List<Map<String, Object>> rawQuestions = objectMapper.readValue(jsonStr, List.class);

            for (Map<String, Object> rawQuestion : rawQuestions) {
                Map<String, Object> question = new HashMap<>();
                question.put("type", type);
                question.put("content", rawQuestion.get("content"));
                question.put("answer", rawQuestion.get("answer"));
                question.put("analysis", rawQuestion.get("analysis"));
                question.put("knowledgePointId", knowledgePointId);

                if (rawQuestion.containsKey("options")) {
                    Object options = rawQuestion.get("options");
                    if (options instanceof List) {
                        question.put("options", objectMapper.writeValueAsString(options));
                    }
                }

                questions.add(question);
            }
        } catch (Exception e) {
            throw new IOException("解析AI生成的题目失败: " + e.getMessage(), e);
        }

        return questions;
    }

    private String extractJsonFromResponse(String response) {
        String cleaned = response.trim();

        int startIdx = cleaned.indexOf('[');
        int endIdx = cleaned.lastIndexOf(']');

        if (startIdx >= 0 && endIdx >= startIdx) {
            return cleaned.substring(startIdx, endIdx + 1);
        }

        return cleaned;
    }

    //AI批改主观题
    public Map<String, Object> gradeSubjectiveQuestion(String questionContent, String correctAnswer, String studentAnswer, int type, int score) throws IOException {
        String typeName = type == 3 ? "填空题" : "简答题";

        String prompt = String.format("""
            请帮我批改%s。

            题目：%s
            参考答案：%s
            学生答案：%s

            该题满分：%d分

            请从以下几个方面进行批改：
            1. 准确性：学生答案是否准确
            2. 完整性：是否涵盖了所有要点
            3. 逻辑清晰度：表达是否清晰
            4. 除简答题外，其他题答案错误便是0份

            请以JSON格式返回结果，只需要JSON，不要其他文字：
            {
                "score": 建议分数（整数）,
                "comment": "批改评语（50-100字），指出优点和不足",
                "isAcceptable": 是否建议接受这个分数（true/false）
            }

            评分标准：
            - %s：答案基本正确即可给满分，有个别错漏扣少量分
            - 简答题：按要点给分，要点齐全给满分，每个缺漏点扣相应分数
            """,
            typeName, questionContent, correctAnswer, studentAnswer, score, typeName);

        String ollamaResponse = callOllama(prompt);
        return parseGradeResponse(ollamaResponse);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGradeResponse(String response) throws IOException {
        try {
            String jsonStr = extractJsonObject(response);
            Map<String, Object> result = objectMapper.readValue(jsonStr, Map.class);

            if (!result.containsKey("score")) {
                result.put("score", 0);
            }
            if (!result.containsKey("comment")) {
                result.put("comment", "AI批改完成");
            }
            if (!result.containsKey("isAcceptable")) {
                result.put("isAcceptable", false);
            }

            return result;
        } catch (Exception e) {
            Map<String, Object> defaultResult = new HashMap<>();
            defaultResult.put("score", 0);
            defaultResult.put("comment", "AI批改结果解析失败，请手动批改。原始回复：" + response);
            defaultResult.put("isAcceptable", false);
            return defaultResult;
        }
    }

    private String extractJsonObject(String response) {
        String cleaned = response.trim();

        int startIdx = cleaned.indexOf('{');
        int endIdx = cleaned.lastIndexOf('}');

        if (startIdx >= 0 && endIdx >= startIdx) {
            return cleaned.substring(startIdx, endIdx + 1);
        }

        return cleaned;
    }
}