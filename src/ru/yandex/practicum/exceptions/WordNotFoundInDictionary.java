package ru.yandex.practicum.exceptions;

import java.io.Serial;

public class WordNotFoundInDictionary extends WordleGameException {
    @Serial
    private static final long serialVersionUID = 4459384982808930284L;

    public WordNotFoundInDictionary(String word) {
        super("Слово '" + word + "' не найдено в словаре!");
    }
}
