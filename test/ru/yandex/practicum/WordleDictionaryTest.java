package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты WordleDictionary")
class WordleDictionaryTest {

    private WordleDictionary dictionary;
    private List<String> testWords;

    @BeforeEach
    void setUp() {
        testWords = Arrays.asList("котте", "домой", "мамам", "папам");
        dictionary = new WordleDictionary(testWords);
    }

    @Nested
    @DisplayName("Тесты валидации слов")
    class ValidationTests {

        @Test
        @DisplayName("Валидное слово из 5 кириллических букв")
        void validWordShouldReturnTrue() {
            assertTrue(WordleDictionary.isValidWord("котте"));
            assertTrue(WordleDictionary.isValidWord("мамам"));
            assertTrue(WordleDictionary.isValidWord("приве"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"abcde", "коттее", "aбвгд", "12345", "котт@", "     "})
        @DisplayName("Невалидные слова")
        void invalidWordsShouldReturnFalse(String word) {
            assertFalse(WordleDictionary.isValidWord(word));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Null и пустые строки невалидны")
        void nullOrEmptyShouldReturnFalse(String word) {
            assertFalse(WordleDictionary.isValidWord(word));
        }

        @Test
        @DisplayName("Слово с буквой ё нормализуется в е")
        void normalizeWithYoShouldReplace() {
            assertEquals("елка", WordleDictionary.normalizeWord("ёлка"));
            assertEquals("мед", WordleDictionary.normalizeWord("мёд"));
        }

        @Test
        @DisplayName("Нормализация удаляет пробелы и приводит к нижнему регистру")
        void normalizeShouldTrimAndLowercase() {
            assertEquals("котте", WordleDictionary.normalizeWord("  КОТТе  "));
            assertEquals("домой", WordleDictionary.normalizeWord("Домой"));
        }
    }

    @Nested
    @DisplayName("Тесты основных методов")
    class CoreMethodsTests {

        @Test
        @DisplayName("Получение размера словаря")
        void sizeShouldReturnCorrectCount() {
            assertEquals(4, dictionary.size());
        }

        @Test
        @DisplayName("Проверка наличия слова в словаре")
        void containsShouldWorkCorrectly() {
            assertTrue(dictionary.contains("котте"));
            assertTrue(dictionary.contains("домой"));
            assertFalse(dictionary.contains("несущ"));
        }

        @Test
        @DisplayName("Получение случайного слова")
        void getRandomWordShouldReturnWordFromDictionary() {
            String randomWord = dictionary.getRandomWord();
            assertTrue(testWords.contains(randomWord));
        }

        @Test
        @DisplayName("Получение копии списка слов - неизменяемый список")
        void getWordsShouldReturnUnmodifiableList() {
            List<String> words = dictionary.getWords();
            assertEquals(testWords, words);
            assertThrows(UnsupportedOperationException.class, () -> words.add("новое"));
        }
    }
}