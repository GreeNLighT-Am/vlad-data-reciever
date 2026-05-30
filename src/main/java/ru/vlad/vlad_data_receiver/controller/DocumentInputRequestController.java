package ru.vlad.vlad_data_receiver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
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

    @PostMapping(value = "/incoming_message",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> receiveIncomingMessage(
            @RequestBody String xmlRequest,
            HttpServletRequest servletRequest) {

        log.info("Получен запрос от: {}", servletRequest.getRemoteAddr());
        log.debug("XML запрос: {}", xmlRequest);

        String response = documentInputRequestService.processRequest(xmlRequest);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(response);
    }
}