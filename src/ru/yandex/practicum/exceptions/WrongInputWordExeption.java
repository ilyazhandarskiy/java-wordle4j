package ru.yandex.practicum.exceptions;

import java.io.Serial;

public class WrongInputWordExeption extends WordleGameException {
    @Serial
    private static final long serialVersionUID = 8105270992460117364L;

    public WrongInputWordExeption(String word) {
        super("Слово '" + word + "' невалидно!");
    }
}
