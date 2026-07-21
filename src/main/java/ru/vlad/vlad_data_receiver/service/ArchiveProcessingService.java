package ru.vlad.vlad_data_receiver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.vlad.vlad_data_receiver.repository.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.exceptions.FileSavingException;
import ru.vlad.vlad_data_receiver.parser.documents.Document;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class ArchiveProcessingService {
    @Value("${app.storage.base-path}")
    private String basePath;
    @Value("${app.storage.date-format}")
    private String datePattern;

    private static final String ARCHIVE_FORMAT = "zip";

    public void process(Long savedBundleId, List<DocumentEntity> savedDocuments, List<Document> allDocumentsFromRequest,
                        String docCategory, LocalDate documentOperationalDayDate) {
        String currentDate = documentOperationalDayDate.format(DateTimeFormatter.ofPattern(datePattern));
        Path storagePath = Paths.get(basePath, currentDate, docCategory);

        String zipFileName = String.format("%d.%s", savedBundleId, ARCHIVE_FORMAT);
        Path zipPath = storagePath.resolve(zipFileName);

        try (OutputStream outputStream = Files.newOutputStream(zipPath);
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

            createDirectories(storagePath);
            log.info("Создан архив: {}", zipFileName);

            for (int i = 0; i < allDocumentsFromRequest.size(); i++) {
                addFileToArchive(zipOutputStream, savedDocuments.get(i), allDocumentsFromRequest.get(i));
            }

            zipOutputStream.finish();
            log.info("Архив {} сохранён по пути: {}", zipFileName, zipPath);
        } catch (IOException e) {
            throw new FileSavingException("Не удалось создать ZIP-архив: " + e.getMessage(), e);
        }
    }

    private void addFileToArchive(ZipOutputStream zipOutputStream, DocumentEntity savedDocument, Document documentFromRequest) throws IOException {
        String extension = savedDocument.getFormat().toLowerCase();
        String fileName = String.format("%d.%s", savedDocument.getId(), extension);

        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(documentFromRequest.getDocumentBody().getContent().get(0).getData());

        zipOutputStream.closeEntry();
        log.debug("В архив добавлен файл: {}", fileName);
    }

    private void createDirectories(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.debug("Создана директория: {}", path);
        }
    }
}