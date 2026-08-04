package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlad.vlad_data_receiver.parser.XmlParserService;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;
import ru.vlad.vlad_data_receiver.validator.DocumentInputRequestValidator;

@Service
@RequiredArgsConstructor
public class DocumentInputRequestService {
    private final XmlParserService xmlParserService;
    private final DocumentInputRequestValidator documentInputRequestValidator;
    private final BundleProcessingService bundleProcessingService;

    public String processRequest(String xmlRequest) {
        DocumentInputRequest document = xmlParserService.parseXml(xmlRequest);
        documentInputRequestValidator.validate(document);
        bundleProcessingService.processBundle(document);

        return document.getID();
    }
}