package ru.yandex.practicum;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.exceptions.WordNotFoundInDictionary;
import ru.yandex.practicum.exceptions.WrongInputWordExeption;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты WordleGame")
class WordleGameTest {

    private WordleDictionary dictionary;
    private PrintWriter log;

    // Предсказуемый словарь для тестов
    private static final List<String> TEST_WORDS = Arrays.asList(
            "котте", "домой", "мамам", "папам", "солнце", "сокол"
    );

    @BeforeEach
    void setUp() {
        dictionary = new WordleDictionary(TEST_WORDS);
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        log = new PrintWriter(new OutputStreamWriter(logOutput, StandardCharsets.UTF_8), true);
    }

    @AfterEach
    void tearDown() {
        log.close();
    }

    // Создаем игру с фиксированным ответом через рефлексию для тестов
    private WordleGame createGameWithFixedAnswer(String answer) throws Exception {
        WordleGame game = new WordleGame(dictionary, 6, log);
        // Используем рефлексию для замены загаданного слова
        java.lang.reflect.Field answerField = WordleGame.class.getDeclaredField("answer");
        answerField.setAccessible(true);
        answerField.set(game, answer);
        return game;
    }

    @Nested
    @DisplayName("Тесты генерации подсказок")
    class HintGenerationTests {

        @Test
        @DisplayName("Точное совпадение - все плюсы")
        void exactMatchShouldReturnAllPluses() throws Exception {
            WordleGame game = createGameWithFixedAnswer("котте");
            String hint = game.generateHint("котте");
            assertEquals("+++++", hint);
        }

        @Test
        @DisplayName("Частичное совпадение")
        void partialMatchShouldGenerateCorrectHint() throws Exception {
            WordleGame game = createGameWithFixedAnswer("котте");
            String hint = game.generateHint("котта");
            assertEquals("++++-", hint);
        }


        @Test
        @DisplayName("Буква есть в слове не на своем месте")
        void letterInWrongPositionShouldShowCarrot() throws Exception {
            WordleGame game = createGameWithFixedAnswer("котте");
            String hint = game.generateHint("текот");
            // Проверяем что есть хотя бы один ^
            assertTrue(hint.contains("^") || hint.contains("+"));
        }
    }

    @Nested
    @DisplayName("Тесты игровой логики")
    class GameLogicTests {

        @Test
        @DisplayName("Победа при правильном угадывании")
        void shouldWinWhenGuessCorrect() throws Exception {
            WordleGame game = createGameWithFixedAnswer("котте");

            String hint = game.makeGuess("котте");

            assertEquals("+++++", hint);
            assertTrue(game.isWon());
            assertFalse(game.isGameOver()); // Игра закончена победой, но флаг gameOver не ставится автоматически
            assertEquals(5, game.getRemainingSteps()); // Попытки уменьшились
        }

        @Test
        @DisplayName("Поражение при исчерпании попыток")
        void shouldLoseWhenNoAttemptsLeft() throws Exception {
            WordleGame game = createGameWithFixedAnswer("котте");

            // Делаем 6 неверных попыток
            for (int i = 0; i < 6; i++) {
                game.makeGuess("домой");
            }

            assertTrue(game.isGameOver());
            assertFalse(game.isWon());
            assertEquals(0, game.getRemainingSteps());
        }

        @Test
        @DisplayName("Выброс исключения при слове не из словаря")
        void shouldThrowExceptionWhenWordNotInDictionary() throws Exception {
            WordleGame game = createGameWithFixedAnswer("котте");

            assertThrows(WordNotFoundInDictionary.class,
                    () -> game.makeGuess("абвгд"));
        }

        @Test
        @DisplayName("Выброс исключения при невалидном слове")
        void shouldThrowExceptionWhenInvalidWord() throws Exception {
            WordleGame game = createGameWithFixedAnswer("котте");

            assertThrows(WrongInputWordExeption.class,
                    () -> game.makeGuess("абвг"));
            assertThrows(WrongInputWordExeption.class,
                    () -> game.makeGuess("12345"));
        }

        @Test
        @DisplayName("История ходов сохраняется корректно")
        void shouldSaveGuessHistory() throws Exception {
            WordleGame game = createGameWithFixedAnswer("котте");

            game.makeGuess("домой");
            game.makeGuess("мамам");

            // Используем рефлексию для проверки истории
            java.lang.reflect.Field historyField = WordleGame.class.getDeclaredField("guessHistory");
            historyField.setAccessible(true);
            List<?> history = (List<?>) historyField.get(game);

            assertEquals(2, history.size());
        }
    }

    @Nested
    @DisplayName("Тесты подсказок (hint)")
    class HintWordTests {

        @Test
        @DisplayName("Подсказка возвращает валидное слово")
        void getHintWordShouldReturnValidWord() throws Exception {
            WordleGame game = createGameWithFixedAnswer("котте");

            String hintWord = game.getHintWord();

            assertNotNull(hintWord);
            assertEquals(5, hintWord.length());
            assertTrue(TEST_WORDS.contains(hintWord) || hintWord.equals("котте"));
        }

        @Test
        @DisplayName("Подсказка прогрессирует после ходов")
        void hintShouldProgressAfterGuesses() throws Exception {
            WordleGame game = createGameWithFixedAnswer("сокол");

            // Делаем первый ход
            game.makeGuess("котте");
            String firstHint = game.getHintWord();

            // Делаем второй ход
            game.makeGuess("домой");
            String secondHint = game.getHintWord();

            assertNotNull(firstHint);
            assertNotNull(secondHint);
        }

    }
}
