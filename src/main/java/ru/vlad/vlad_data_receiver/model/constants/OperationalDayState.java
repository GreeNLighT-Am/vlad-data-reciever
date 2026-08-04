package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationalDayState {
    UNLOADING_RECEIVE_AVAILABLE(1),
    UNLOADING_RECEIVE_STOPPED(2);

    private final int stateId;
}