package ru.yandex.practicum;

import java.util.*;
import java.util.regex.Pattern;

/*
    этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    private static final Pattern CYRILLIC_PATTERN = Pattern.compile("^[\\u0400-\\u04FF]+$");
    private final List<String> words;
    private final Random random;
    private static final int NUM_LETTERS_IN_WORD = 5;

    public WordleDictionary(List<String> words) {
        this.words = List.copyOf(words);
        random = new Random();
    }

    public int size() {
        return words.size();
    }

    public boolean contains(String word) {
        return words.contains(word);
    }

    // Возвращает случайное слово из словаря.
    public String getRandomWord() {
        return words.get(random.nextInt(words.size()));
    }

    // возвращает список (копию для безопасности)
    public List<String> getWords() {
        return List.copyOf(words);
    }

    /* Метод валидации слова
        1. состоит из 5 букв
        2. только символы кирилицы
        3. не пустая строка, пробелы и null
     */

    public static boolean isValidWord(String word) {
        if (word == null) {
            return false;
        }
        word = word.trim();
        if (word.isBlank()) {
            return false;
        }
        if (word.length() != NUM_LETTERS_IN_WORD) {
            return false;
        }
        return CYRILLIC_PATTERN.matcher(word).matches();
    }

    //метод нормализации слова
    public static String normalizeWord(String word) {
        word = word.trim();
        return word.toLowerCase().replace('ё', 'е');
    }

}
