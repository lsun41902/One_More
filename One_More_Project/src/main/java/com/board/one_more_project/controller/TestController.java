package com.board.one_more_project.controller;

import com.board.one_more_project.service.AiClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// 프로젝트 첫 생성 후 테스트용.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {
//    private final AiClientService aiClientService;
//
//    @PostMapping("/send")
//    public String test(@RequestBody List<String> ingredients) {
//        return aiClientService.sendIngredients(ingredients);
//    }
//
//    @PostMapping("/send-image")
//    public String testImage(@RequestParam("file") MultipartFile file) {
//        return aiClientService.sendImage(file);
//    }
}

