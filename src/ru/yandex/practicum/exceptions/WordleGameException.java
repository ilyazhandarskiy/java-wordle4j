package ru.yandex.practicum.exceptions;

import java.io.Serial;

public class WordleGameException extends Exception{

    @Serial
    private static final long serialVersionUID = -8077749631138257839L;

    public WordleGameException(String message) {
        super(message);
    }
}
