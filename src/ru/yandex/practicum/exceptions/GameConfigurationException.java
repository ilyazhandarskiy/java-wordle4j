package ru.yandex.practicum.exceptions;

//Непроверяемое исключение в случае фатальных сбоев при конфигурации игры

import java.io.Serial;

public class GameConfigurationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 4838860807183195381L;

    public GameConfigurationException(String message) {
        super(message);
    }

    public GameConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
