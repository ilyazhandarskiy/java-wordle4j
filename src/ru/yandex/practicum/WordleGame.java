package ru.yandex.practicum;

import ru.yandex.practicum.exceptions.WordNotFoundInDictionary;
import ru.yandex.practicum.exceptions.WordleGameException;
import ru.yandex.practicum.exceptions.WrongInputWordExeption;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */


public class WordleGame {

    private final String answer;                // Загаданное слово
    private int steps;                          // Оставшиеся попытки
    private final WordleDictionary dictionary;  // Словарь слов
    private boolean gameOver = false;           // Флаг окончания игры
    private boolean won = false;                // Флаг победы
    private final PrintWriter log;              // Логгер событий

    private final List<GuessRecord> guessHistory = new ArrayList<>(); // История всех ходов

    // Класс для хранения ходов
    private record GuessRecord(String word, String hint) {
    }

    public WordleGame(WordleDictionary dictionary, int steps, PrintWriter log) {
        this.answer = dictionary.getRandomWord();
        this.steps = steps;
        this.dictionary = dictionary;
        this.log = log;
        log.printf("[INFO] Запуск новой игры! Загадано слово: \"%s\" %s\n", answer, LocalDateTime.now());
    }

    // Обработка хода игрока
    public String makeGuess(String word) throws WordNotFoundInDictionary, WrongInputWordExeption {
        // Нормализуем ввод
        word = WordleDictionary.normalizeWord(word);

        // Проверка валидности слова
        if (!WordleDictionary.isValidWord(word)) {
            log.printf("[WARNING] Слово \"%s\" невалидно %s\n", word, LocalDateTime.now());
            throw new WrongInputWordExeption(word);
        }

        // Проверка на победу
        if (word.equals(answer)) {
            this.won = true;
        } else if (!dictionary.contains(word)) { // Проверка наличия слова в словаре
            log.printf("[WARNING] Слово \"%s\" отсутствует в словаре %s\n", word, LocalDateTime.now());
            throw new WordNotFoundInDictionary(word);
        }

        String hint = generateHint(word); // Генерируем подсказку
        guessHistory.add(new GuessRecord(word, hint)); // Сохраняем в историю
        steps--; // Уменьшаем количество попыток

        log.printf("[INFO] Ход: %s -> %s (осталось попыток: %s) %s\n", word, hint, steps, LocalDateTime.now());

        // Проверка окончания игры
        if (steps == 0) {
            this.gameOver = true;
        }

        if (isWon()) {
            log.printf("[INFO] Игрок победил. Было загадано слово: %s %s\n", answer, LocalDateTime.now());
        }

        if (isGameOver()) {
            log.printf("[INFO] У игрока закончились ходы. Было загадано слово: %s %s\n", answer, LocalDateTime.now());
        }

        return hint;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }

    public int getRemainingSteps() {
        return steps;
    }

    public String getAnswer() {
        return answer;
    }

    // Генерация подсказки
    public String generateHint(String guess) {
        int len = answer.length();
        StringBuilder hint = new StringBuilder(len);
        boolean[] usedInAnswer = new boolean[len]; // Отмечаем уже использованные буквы ответа

        // Пишем в hint прочерки как начальное значение подсказки
        hint.repeat("-", len);

        // Первый проход: ищем точные совпадения '+'
        for (int i = 0; i < len; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                hint.setCharAt(i, '+');
                usedInAnswer[i] = true;
            }
        }

        // Второй проход: ищем буквы '^' (есть в слове, но не на этом месте)
        for (int i = 0; i < len; i++) {
            if (hint.charAt(i) == '-') {
                char c = guess.charAt(i);
                for (int j = 0; j < len; j++) {
                    if (!usedInAnswer[j] && answer.charAt(j) == c) {
                        hint.setCharAt(i, '^');
                        usedInAnswer[j] = true;
                        break;
                    }
                }
            }
        }

        return hint.toString();
    }

    // Предоставляет слово-подсказку
    public String getHintWord() throws WordleGameException {
        // Находим максимальное количество плюсов среди всех подсказок в истории
        int maxPlusCount = getMaxPlusCountFromHistory();

        // Пытаемся найти слово, увеличивая количество плюсов от maxPlusCount+1 до длины слова
        for (int targetPlusCount = maxPlusCount + 1; targetPlusCount <= answer.length(); targetPlusCount++) {
            String result = findFirstCandidate(targetPlusCount);
            if (result != null) {
                log.printf("[INFO] Подсказка: %s (плюсов: %d, максимально было: %d) %s\n",
                        result, targetPlusCount, maxPlusCount, LocalDateTime.now());
                return result;
            }
        }

        // Если ничего не нашли даже с максимальным количеством плюсов - исключение
        // (но такого быть не должно по идее, так как слово загадано из словаря)
        log.printf("[ERROR] Не найдено подходящее слово-подсказка %s\n", LocalDateTime.now());
        throw new WordleGameException("Подсказки отсуствуют.");
    }

    // Находит максимальное количество плюсов среди всех подсказок в истории
    private int getMaxPlusCountFromHistory() {
        if (guessHistory.isEmpty()) {
            return 0;
        }

        int maxPlus = 0;
        for (GuessRecord record : guessHistory) {
            int plusCount = countPlus(record.hint);
            if (plusCount > maxPlus) {
                maxPlus = plusCount;
            }
        }
        return maxPlus;
    }

    // Поиск первого подходящего кандидата с заданным количеством плюсов
    private String findFirstCandidate(int targetPlusCount) {
        for (String word : dictionary.getWords()) {
            String normalized = WordleDictionary.normalizeWord(word);

            if (isWordUsed(normalized)) continue;      // Пропускаем уже использованные слова
            if (!fitsAllHistory(normalized)) continue; // Проверяем совместимость с историей

            // Проверяем количество плюсов
            if (countPlus(generateHint(normalized)) == targetPlusCount) {
                return normalized;
            }
        }
        return null;
    }

    // Проверка, использовалось ли слово ранее
    private boolean isWordUsed(String word) {
        for (GuessRecord record : guessHistory) {
            if (record.word.equals(word)) {
                return true;
            }
        }
        return false;
    }

    // Проверяет, подходит ли кандидат под всю историю игры
    private boolean fitsAllHistory(String candidate) {
        for (GuessRecord record : guessHistory) {
            if (!fitsGuess(candidate, record.word, record.hint)) {
                return false;
            }
        }
        return true;
    }

    // Проверяет соответствие кандидата одному конкретному ходу.
    // Анализируем только символы '+', '^' и '-' игнорируются
    private boolean fitsGuess(String candidate, String guess, String hint) {
        int len = answer.length();

        for (int i = 0; i < len; i++) {
            if (hint.charAt(i) == '+' && candidate.charAt(i) != guess.charAt(i)) {
                return false; // Не совпадает буква на позиции, где должно быть точное совпадение
            }
        }
        return true;
    }

    // Подсчёт количества символов '+' в подсказке
    private int countPlus(String hint) {
        int count = 0;
        for (int i = 0; i < hint.length(); i++) {
            if (hint.charAt(i) == '+') count++;
        }
        return count;
    }
}