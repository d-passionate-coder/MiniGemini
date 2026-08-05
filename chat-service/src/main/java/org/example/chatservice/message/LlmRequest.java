package org.example.chatservice.message;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record LlmRequest(List<Content> contents) {
    public record Content(String role,List<Part> parts) {};
    public record Part(String text) {};

    public static LlmRequest fromHistory(List<Map<String,String>> history){
       List<Content> contents = history.stream()
               .map(msg -> {
                   Part part = new Part(msg.get("content"));

                   return new Content(msg.get("role"),List.of(part));
               })
               .collect(Collectors.toList());

       return new LlmRequest(contents);
    }


    public static LlmRequest fromSinglePrompt(String prompt){
        Part part = new Part(prompt);
        List<Content> contents = List.of(new Content("user",List.of(part)));
        return new LlmRequest(contents);
    }
}
