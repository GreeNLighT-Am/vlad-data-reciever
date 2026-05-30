package ru.vlad.vlad_data_receiver.service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import ru.vlad.vlad_data_receiver.model.DocumentInputRequest;
import ru.vlad.vlad_data_receiver.parser.filter.NamespaceFilter;

import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentInputRequestService {

    private final JAXBContext jaxbContext;

    public String processRequest(String xmlRequest) {
        try {
            DocumentInputRequest request = parseXml(xmlRequest);

            log.info("""
                            Успешно разобран Document_Input_Request
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

            return "<Response><Status>SUCCESS</Status><Message>Запрос успешно обработан</Message></Response>";
        } catch (JAXBException e) {
            log.error("Ошибка парсинга XML: {}", e.getMessage(), e);
            return "<Error><Status>PARSE_ERROR</Status><Message>Invalid XML format: " +
                    e.getMessage() + "</Message></Error>";
        } catch (SAXException e) {
            log.error("Ошибка обработки XML: {}", e.getMessage(), e);
            return "<Error><Status>XML_PROCESSING_ERROR</Status><Message>" +
                    e.getMessage() + "</Message></Error>";
        } catch (RuntimeException e) {
            log.error("Неожиданная ошибка выполнения: {}", e.getMessage(), e);
            return "<Error><Status>INTERNAL_ERROR</Status><Message>Internal server error</Message></Error>";
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
