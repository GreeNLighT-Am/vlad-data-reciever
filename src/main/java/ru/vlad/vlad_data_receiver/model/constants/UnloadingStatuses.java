package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnloadingStatuses {
    BAD_UNLOADING(-1L),
    GOOD_UNLOADING(1L);

    private final Long status;
}