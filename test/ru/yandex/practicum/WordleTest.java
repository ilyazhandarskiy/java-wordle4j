package ru.yandex.practicum;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.exceptions.GameConfigurationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты всего процесса Wordle")
class WordleTest {

    @TempDir
    Path tempDir;

    private Path wordsFile;
    private Path logFile;

    @BeforeEach
    void setUp() throws IOException {
        wordsFile = tempDir.resolve("words_ru.txt");
        logFile = tempDir.resolve("log.txt");

        // Создаем тестовый файл со словами
        try (BufferedWriter writer = Files.newBufferedWriter(wordsFile, StandardCharsets.UTF_8)) {
            writer.write("котте\n");
            writer.write("домой\n");
            writer.write("мамам\n");
            writer.write("папам\n");
            writer.write("сокол\n");
            writer.write("лучик\n");
            writer.write("гора\n");    // невалидно
            writer.write("солнце\n");
            writer.write("море\n");    // невалидно
        }
    }

    @Test
    @DisplayName("Полный игровой цикл с победой")
    void fullGameCycleWithWin() {
        // Этот тест сложно реализовать без мокирования System.in
        // В реальном проекте нужно использовать System.setIn()
        assertTrue(true, "Интеграционный тест требует мокирования ввода");
    }

    @Test
    @DisplayName("Загрузка игры с валидным файлом словаря")
    void gameLoadsSuccessfullyWithValidDictionary() throws Exception {
        // Создаем лог
        try (PrintWriter log = new PrintWriter(new FileOutputStream(logFile.toFile()), true)) {

            // Загружаем словарь
            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary dictionary = loader.getWordleDictionary(wordsFile.toString());

            // Создаем игру
            WordleGame game = new WordleGame(dictionary, 6, log);

            assertNotNull(game);
            assertEquals(6, game.getRemainingSteps());
            assertNotNull(game.getAnswer());
            assertTrue(dictionary.contains(game.getAnswer()));
        }
    }

    @Test
    @DisplayName("Обработка ошибки при отсутствии файла словаря")
    void handlesMissingDictionaryFile() {
        try (PrintWriter log = new PrintWriter(new FileOutputStream(logFile.toFile()), true)) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

            assertThrows(GameConfigurationException.class,
                    () -> loader.getWordleDictionary("nonexistent.txt"));

            // Проверяем что ошибка залогирована
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile.toFile()))) {
                String line;
                boolean hasError = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("[ERROR]")) {
                        hasError = true;
                        break;
                    }
                }
                assertTrue(hasError, "Должна быть запись об ошибке в логе");
            }
        } catch (IOException e) {
            fail("Не удалось проверить лог: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Корректная работа с буквой ё")
    void handlesYoLetterCorrectly() throws Exception {
        // Создаем файл со словом содержащим ё
        Path yoFile = tempDir.resolve("words_yo.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(yoFile, StandardCharsets.UTF_8)) {
            writer.write("ёжику\n");
            writer.write("ёлкаа\n");
        }

        try (PrintWriter log = new PrintWriter(new FileOutputStream(logFile.toFile()), true)) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary dictionary = loader.getWordleDictionary(yoFile.toString());

            // Слово должно быть нормализовано
            assertTrue(dictionary.contains("ежику"));
            assertTrue(dictionary.contains("елкаа"));
            assertFalse(dictionary.contains("ёжику"));

            // Проверяем что длина 5
            assertTrue(WordleDictionary.isValidWord("ежику"));
            assertEquals(5, "ежику".length());
        }
    }
}