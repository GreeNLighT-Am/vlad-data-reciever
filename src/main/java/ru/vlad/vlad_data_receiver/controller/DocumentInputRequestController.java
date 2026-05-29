package ru.vlad.vlad_data_receiver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import ru.vlad.vlad_data_receiver.filter.NamespaceFilter;
import ru.vlad.vlad_data_receiver.model.DocumentInputRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DocumentInputRequestController {

    private final JAXBContext jaxbContext;

    @PostMapping(value = "/incoming_message",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> receiveIncomingMessage(
            @RequestBody String xmlRequest,
            HttpServletRequest servletRequest) {

        log.info("Received request from: {}", servletRequest.getRemoteAddr());
        log.debug("Request XML: {}", xmlRequest);

        try {
            DocumentInputRequest request = parseXml(xmlRequest);

            log.info("""
                            Successfully parsed Document_Input_Request
                            ID: {}
                            TimeStamp: {}
                            blockNum: {}
                            totalDocs: {}
                            dataSet: {}
                            odDocType: {}
                            Documents count: {}""",
                    request.getID(), request.getTimeStamp(), request.getBlockNum(),
                    request.getTotalDocs(), request.getDataSet(), request.getOdDocType(),
                    request.getDocument().size());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body("<Response><Status>SUCCESS</Status><Message>Request processed successfully</Message></Response>");
        } catch (JAXBException e) {
            log.error("Error parsing XML: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(
                            "<Error><Status>PARSE_ERROR</Status><Message>Invalid XML format: " +
                                    e.getMessage() + "</Message></Error>"
                    );
        } catch (SAXException e) {
            log.error("XML processing error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(
                            "<Error><Status>XML_PROCESSING_ERROR</Status><Message>" +
                                    e.getMessage() + "</Message></Error>"
                    );
        } catch (RuntimeException e) {
            log.error("Unexpected runtime error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(
                            "<Error><Status>INTERNAL_ERROR</Status><Message>Internal server error</Message></Error>"
                    );
        }
    }

    private DocumentInputRequest parseXml(String xml) throws JAXBException, SAXException {
        InputSource source = new InputSource(new StringReader(xml));
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        NamespaceFilter filter = new NamespaceFilter("http://ru.vlad/documents", true);
        filter.setParent(xmlReader);
        SAXSource saxSource = new SAXSource(filter, source);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (DocumentInputRequest) unmarshaller.unmarshal(saxSource);
    }
}