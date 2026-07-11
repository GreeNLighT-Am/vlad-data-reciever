package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnloadingStates {
    UNLOADING_ERROR(-1L),
    NEW_UNLOADING(1L),
    UNLOADING_SAVED(2L);

    private final Long stateId;
}