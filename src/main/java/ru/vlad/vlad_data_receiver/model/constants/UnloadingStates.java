package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnloadingStates {
    UNLOADING_ERROR(-1),
    NEW_UNLOADING(1),
    UNLOADING_SAVED(2);

    private final int stateId;
}