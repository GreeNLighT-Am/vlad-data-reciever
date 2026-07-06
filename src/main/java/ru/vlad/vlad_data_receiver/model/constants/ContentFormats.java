package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentFormats {
    TXT("txt"),
    TIF("tif"),
    JPEG("jpeg"),
    JPG("jpg"),
    PDF("pdf"),
    ZIP("zip"),
    XML("xml"),
    XLS("xls"),
    XLSX("xlsx"),
    DOC("doc"),
    DOCX("docx");

    private final String value;

    public static boolean isValid(String format) {
        for (ContentFormats contentFormats : values()) {
            if (contentFormats.value.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }
}