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
import ru.vlad.vlad_data_receiver.model.constants.UnloadingStates;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;
import ru.vlad.vlad_data_receiver.parser.filter.NamespaceFilter;
import ru.vlad.vlad_data_receiver.service.UnloadingCrudService;

import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            log.debug("""
                            Успешно разобран Document_Input_Request
                            ID: {}
                            TimeStamp: {}
                            blockNum: {}
                            totalDocs: {}
                            dataSet: {}
                            odDocType: {}
                            Documents count: {}""",
                    document.getID(), document.getTimeStamp(), document.getBlockNum(),
                    document.getTotalDocs(), document.getDataSet(), document.getOdDocType(),
                    document.getDocument().size());

            return document;
        } catch (SAXException | JAXBException | NumberFormatException e) {
            String id = extractUnloadingIdFromXml(xml);

            if (id != null && !id.isBlank()) {
                int inserted = unloadingCrudService.setUnloadingStateId(id, UnloadingStates.UNLOADING_ERROR);
                if (inserted > 0) {
                    log.error("{}. Для запроса с ID={} в БД создана выгрузка со статусом -1", ERROR_MESSAGE, id);
                } else {
                    log.error("{}. Для запроса с ID={} в БД не создана выгрузка со статусом -1 т.к. выгрузка с таким ID уже существует", ERROR_MESSAGE, id);
                }
            } else {
                log.error("{}. Для текущего запроса в БД не создана выгрузка со статусом -1 т.к. не удалось извлечь ID из XML", ERROR_MESSAGE);
            }
            throw new XmlParsingException(ERROR_MESSAGE, e);
        }
    }

    private String extractUnloadingIdFromXml(String xml) {
        if (xml == null || xml.isBlank()) {
            return null;
        }

        String[] patterns = {
                "<Document_Input_Request[^>]*\\bID\\s*=\\s*[\"']([^\"']+)[\"']",
                "\\bID\\s*=\\s*[\"']([^\"']+)[\"']",
                "<ID\\s*>(.*?)</ID\\s*>",
                "\\bid\\s*=\\s*[\"']([^\"']+)[\"']"
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);
            if (matcher.find()) {
                String id = matcher.group(1).trim();
                if (!id.isEmpty()) {
                    log.debug("Извлечён ID из XML (по паттерну '{}'): {}", patternStr, id);
                    return id;
                }
            }
        }

        Pattern uuidPattern = Pattern.compile(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
                Pattern.CASE_INSENSITIVE
        );
        Matcher uuidMatcher = uuidPattern.matcher(xml);
        if (uuidMatcher.find()) {
            String possibleId = uuidMatcher.group();
            log.debug("Найден возможный UUID в XML: {}", possibleId);
            return possibleId;
        }

        return null;
    }
}