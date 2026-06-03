package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.vlad.vlad_data_receiver.parser.XmlParserService;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;

@Service
@RequiredArgsConstructor
public class DocumentInputRequestService {

    private final XmlParserService xmlParserService;

    public String processRequest(String xmlRequest) {
        DocumentInputRequest document = xmlParserService.parseXml(xmlRequest);

        return document.getID();
    }
}
