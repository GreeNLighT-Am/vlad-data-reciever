package ru.vlad.vlad_data_receiver.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vlad.vlad_data_receiver.service.DocumentInputRequestService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DocumentInputRequestController {

    private final DocumentInputRequestService documentInputRequestService;

    @PostMapping("/incoming_message")
    public ResponseEntity<String> receiveIncomingMessage(
            @RequestBody String xmlRequest,
            HttpServletRequest servletRequest) {

        log.info("Получен запрос от: {}", servletRequest.getRemoteAddr());
        log.debug("Тело запроса: {}", xmlRequest);

        String documentId = documentInputRequestService.processRequest(xmlRequest);

        return ResponseEntity.ok().body(String.format("Запрос Document_Input_Request с ID: %s успешно обработан", documentId));
    }
}