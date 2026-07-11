package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BundleStates {
    NEW_BUNDLE("NEW_BUNDLE"),
    SAVING_METADATA_ERROR("SAVING_METADATA_ERROR"),
    SAVING_FILER_ERROR("SAVING_FILER_ERROR"),
    BUNDLE_SAVED("BUNDLE_SAVED");

    private final String status;
}