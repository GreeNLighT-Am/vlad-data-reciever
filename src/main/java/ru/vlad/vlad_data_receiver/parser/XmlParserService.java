package ru.vlad.vlad_data_receiver.parser;

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
import ru.vlad.vlad_data_receiver.exceptions.XmlParsingException;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;
import ru.vlad.vlad_data_receiver.parser.filter.NamespaceFilter;
import ru.vlad.vlad_data_receiver.service.UnloadingCrudService;

import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class XmlParserService {
    private final JAXBContext jaxbContext;
    private final UnloadingCrudService unloadingCrudService;
    private static final String ERROR_MESSAGE = "Ошибка при парсинге XML";

    public DocumentInputRequest parseXml(String xml) {
        try {
            InputSource source = new InputSource(new StringReader(xml));
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            NamespaceFilter filter = new NamespaceFilter("http://ru.vlad/documents", true);
            filter.setParent(xmlReader);
            SAXSource saxSource = new SAXSource(filter, source);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            DocumentInputRequest document = (DocumentInputRequest) unmarshaller.unmarshal(saxSource);

//            log.debug("""
//                            Успешно разобран Document_Input_Request
//                            ID: {}
//                            TimeStamp: {}
//                            blockNum: {}
//                            totalDocs: {}
//                            dataSet: {}
//                            odDocType: {}
//                            Documents count: {}""",
//                    document.getID(), document.getTimeStamp(), document.getBlockNum(),
//                    document.getTotalDocs(), document.getDataSet(), document.getOdDocType(),
//                    document.getDocument().size());

            return document;
        } catch (SAXException | JAXBException | NumberFormatException e) {
            String errorMessage = "Ошибка при парсинге XML";
            log.error(errorMessage, e);
            throw new XmlParsingException(errorMessage, e);
        }
    }
}