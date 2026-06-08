package ru.yandex.practicum;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.exceptions.GameConfigurationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты WordleDictionaryLoader")
class WordleDictionaryLoaderTest {

    @TempDir
    Path tempDir;

    private PrintWriter log;
    private ByteArrayOutputStream logOutput;
    private WordleDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        logOutput = new ByteArrayOutputStream();
        log = new PrintWriter(new OutputStreamWriter(logOutput, StandardCharsets.UTF_8), true);
        loader = new WordleDictionaryLoader(log);
    }

    @AfterEach
    void tearDown() {
        log.close();
    }

    @Test
    @DisplayName("Успешная загрузка валидных слов из файла")
    void shouldLoadValidWordsSuccessfully() throws Exception {
        // Создаем тестовый файл
        Path dictFile = tempDir.resolve("words.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(dictFile, StandardCharsets.UTF_8)) {
            writer.write("котте\n");
            writer.write("домой\n");
            writer.write("мамам\n");
            writer.write("невалид\n"); // не 5 букв
            writer.write("    \n");    // пустая строка
            writer.write("ёжику\n");   // с ё
        }

        WordleDictionary dictionary = loader.getWordleDictionary(dictFile.toString());

        assertNotNull(dictionary);
        assertEquals(4, dictionary.size()); // только 4 валидных слова
        assertTrue(dictionary.contains("котте"));
        assertTrue(dictionary.contains("домой"));
        assertTrue(dictionary.contains("мамам"));
        assertTrue(dictionary.contains("ежику")); // ё нормализовалась в е
    }

    @Test
    @DisplayName("Выброс исключения при пустом файле")
    void shouldThrowExceptionWhenFileIsEmpty() throws Exception {
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);

        GameConfigurationException exception = assertThrows(
                GameConfigurationException.class,
                () -> loader.getWordleDictionary(emptyFile.toString())
        );

        assertTrue(exception.getMessage().contains("отсутствуют подходящие слова"));
    }

    @Test
    @DisplayName("Выброс исключения при отсутствии файла")
    void shouldThrowExceptionWhenFileNotFound() {
        GameConfigurationException exception = assertThrows(
                GameConfigurationException.class,
                () -> loader.getWordleDictionary("nonexistent_file.txt")
        );

        assertTrue(exception.getMessage().contains("Файл словаря не найден"));
    }

    @Test
    @DisplayName("Логирование при загрузке")
    void shouldLogLoadingInformation() throws Exception {
        Path dictFile = tempDir.resolve("words.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(dictFile, StandardCharsets.UTF_8)) {
            writer.write("котте\n");
            writer.write("домой\n");
        }

        loader.getWordleDictionary(dictFile.toString());

        String logContent = logOutput.toString(StandardCharsets.UTF_8);
        assertTrue(logContent.contains("[INFO] Сформировано 2 слов"));
    }
}

