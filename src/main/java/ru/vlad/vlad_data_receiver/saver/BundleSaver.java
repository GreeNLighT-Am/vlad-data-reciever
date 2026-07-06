package ru.vlad.vlad_data_receiver.saver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.vlad.vlad_data_receiver.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.exceptions.StorageException;
import ru.vlad.vlad_data_receiver.parser.documents.Document;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class BundleSaver {

    @Value("${app.storage.base-path}")
    private String basePath;
    @Value("${app.storage.date-format}")
    private String datePattern;
    @Value("${app.storage.archive-format}")
    private String archiveFormat;

    public void process(Long bundleId, List<DocumentEntity> documentEntities, List<Document> documents, String docCategory) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern(datePattern));
        Path storagePath = Paths.get(basePath, currentDate, docCategory);

        String zipFileName = bundleId + archiveFormat;
        Path zipPath = storagePath.resolve(zipFileName);
        try {
            createDirectories(storagePath);

            try (OutputStream outputStream = Files.newOutputStream(zipPath);
                 ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                log.info("Создан архив: {}", zipFileName);

                for (int i = 0; i < documentEntities.size() && i < documents.size(); i++) {
                    addFileToArchive(zipOutputStream, documentEntities.get(i), documents.get(i));
                }

                zipOutputStream.finish();

                log.info("Архив {} сохранён по пути: {}", zipFileName, zipPath);
            }
        } catch (IOException e) {
            log.error("Ошибка при создании ZIP-архива", e);
            throw new StorageException("Не удалось создать ZIP-архив: " + e.getMessage(), e);
        }
    }

    private void addFileToArchive(ZipOutputStream zipOutputStream, DocumentEntity documentEntity, Document document) throws IOException {
        String extension = documentEntity.getFormat().toLowerCase();
        String fileName = documentEntity.getId() + "." + extension;

        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(document.getDocumentBody().getContent().get(0).getData());

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
