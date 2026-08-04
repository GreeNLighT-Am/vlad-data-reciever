package ru.vlad.vlad_data_receiver.model.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BundleState {
    NEW_BUNDLE(),
    SAVING_METADATA_ERROR(),
    SAVING_FILE_ERROR(),
    BUNDLE_SAVED();
}