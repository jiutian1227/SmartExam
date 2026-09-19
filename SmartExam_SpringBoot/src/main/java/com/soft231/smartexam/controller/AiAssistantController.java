package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.service.AiAssistantService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

//AI助手控制器
//回答学生问题（流式输出） askQuestionStream
@RestController
@RequestMapping("/api/ai-assistant")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    //回答学生问题（流式输出）
    @PostMapping(value = "/ask-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askQuestionStream(@RequestBody Map<String, Object> data) {
        SseEmitter emitter = new SseEmitter(300000L);

        Thread streamingThread = new Thread(() -> {
            try {
                String question = (String) data.get("question");
                String subject = (String) data.get("subject");

                if (question == null || question.trim().isEmpty()) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("问题不能为空"));
                    emitter.complete();
                    return;
                }

                if (question.length() > 1000) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("问题长度不能超过1000字"));
                    emitter.complete();
                    return;
                }

                InputStream inputStream = aiAssistantService.answerQuestionStream(question.trim(), subject);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                
                String line;
                StringBuilder buffer = new StringBuilder();
                
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    
                    buffer.append(line);
                    
                    try {
                        JsonNode jsonNode = objectMapper.readTree(buffer.toString());
                        JsonNode messageNode = jsonNode.get("message");
                        if (messageNode != null) {
                            String content = messageNode.has("content") ? messageNode.get("content").asText() : "";
                            if (!content.isEmpty()) {
                                content = processContent(content);
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(content));
                            }
                            
                            boolean done = jsonNode.has("done") && jsonNode.get("done").asBoolean();
                            if (done) {
                                emitter.send(SseEmitter.event()
                                        .name("complete")
                                        .data("回答完成"));
                                break;
                            }
                        }
                        buffer.setLength(0);
                    } catch (Exception e) {
                    }
                }
                
                reader.close();
                inputStream.close();
                emitter.complete();
                
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        
        streamingThread.start();
        
        return emitter;
    }

    private String processContent(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        content = decodeUnicode(content);
        //只解码Unicode，保留原始Markdown交给前端marked渲染
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
}