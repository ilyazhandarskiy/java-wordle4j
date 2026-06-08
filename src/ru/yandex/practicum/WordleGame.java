package ru.yandex.practicum;

import ru.yandex.practicum.exceptions.WordNotFoundInDictionary;
import ru.yandex.practicum.exceptions.WordleGameException;
import ru.yandex.practicum.exceptions.WrongInputWordExeption;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

public class WordleGame {

    private static final char CORRECT_POSITION = '+';
    private static final char OTHER_POSITION = '^';
    private static final char INCORRECT_POSITION = '-';

    private final String answer;                // Загаданное слово
    private int steps;                          // Оставшиеся попытки
    private final WordleDictionary dictionary;  // Словарь слов
    private boolean gameOver = false;           // Флаг окончания игры
    private boolean won = false;                // Флаг победы
    private final PrintWriter log;              // Логгер событий
    private final Random random = new Random();

    private final List<GuessRecord> guessHistory = new ArrayList<>(); // История всех ходов

    //коллекции для подсказок
    private final Map<Integer, Character> fixedPositions = new HashMap<>(); // Точные позиции букв
    // Буквы, которых не может быть на позиции - для слов с '^'
    private final Map<Integer, Set<Character>> forbiddenPositions = new HashMap<>();
    private final Set<Character> correctLetters = new HashSet<>(); // Буквы, которые должно содержать слово
    private final Set<Character> incorrectLetters = new HashSet<>(); // Буквы, которые НЕ должно содержать слово

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
            won = true;
            gameOver = true;
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
        StringBuilder hint = new StringBuilder(String.valueOf(INCORRECT_POSITION).repeat(len)); // Инициализация прочерками
        boolean[] usedInAnswer = new boolean[len];
        Map<Character, Integer> answerLetterCount = new HashMap<>();

        // Считаем количество каждой буквы в ответе
        for (char c : answer.toCharArray()) {
            answerLetterCount.merge(c, 1, Integer::sum);
        }

        // ПЕРВЫЙ ПРОХОД: точные совпадения '+'
        for (int i = 0; i < len; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                hint.setCharAt(i, CORRECT_POSITION);
                usedInAnswer[i] = true;
                answerLetterCount.merge(guess.charAt(i), -1, Integer::sum);
            }
        }

        // ВТОРОЙ ПРОХОД: буквы '^' (есть, но не на этом месте)
        for (int i = 0; i < len; i++) {
            if (hint.charAt(i) == '-') {
                char c = guess.charAt(i);
                // Проверяем, остались ли ещё неиспользованные вхождения этой буквы
                if (answerLetterCount.getOrDefault(c, 0) > 0) {
                    // Ищем позицию в ответе для этой буквы
                    for (int j = 0; j < len; j++) {
                        if (!usedInAnswer[j] && answer.charAt(j) == c) {
                            hint.setCharAt(i, OTHER_POSITION);
                            usedInAnswer[j] = true;
                            answerLetterCount.merge(c, -1, Integer::sum);
                            break;
                        }
                    }
                }
            }
        }
        //обновляем сеты коллекции по подсказкам
        updateLetterCollections(guess, hint.toString());
        return hint.toString();
    }

    // метод фиксирует списки корректных и некорректных букв в слове
    private void updateLetterCollections(String guess, String hint) {
        for (int i = 0; i < guess.length(); i++) {
            //записали буквы, которые точно на своих местах
            if (hint.charAt(i) == CORRECT_POSITION) {
                fixedPositions.put(i, guess.charAt(i));
                correctLetters.add(guess.charAt(i));
            }

            //добавляем в список корректных букв в слове и некорректных позиций в слове
            if (hint.charAt(i) == OTHER_POSITION) {
                correctLetters.add(guess.charAt(i));
                forbiddenPositions.computeIfAbsent(i, k -> new HashSet<>()).add(guess.charAt(i));
            }
        }

        // делаю второй проход для того чтобы в incorrectLetters добавлять только после
        // того когда определились все correctLetters в кандидате
        for (int i = 0; i < guess.length(); i++) {
            //буквы, которые точно не на своей позиции
            if (hint.charAt(i) == INCORRECT_POSITION) {
                if (!correctLetters.contains(guess.charAt(i))) {
                    incorrectLetters.add(guess.charAt(i));
                }
            }
        }
    }


    // Предоставляет слово-подсказку
    public String getHintWord() throws WordleGameException {

        if (guessHistory.isEmpty()) {
            return dictionary.getRandomWord();
        }

        List<String> allWords = dictionary.getWords();

        // список кандидатов
        List<String> candidateWords = new ArrayList<>();

        for (String candidate : allWords) {
            if (checkWordContainsInHistory(candidate)) {
                continue;
            }

            if (checkCandidatesWordMatch(candidate)) {
                candidateWords.add(candidate);
            }
        }

        if (candidateWords.isEmpty()) {
            log.printf("[ERROR] Не найдено подходящее слово-подсказка %s\n", LocalDateTime.now());
            throw new WordleGameException("Подсказки отсуствуют.");
        }

        String result = candidateWords.get(random.nextInt(candidateWords.size()));
        log.printf("[INFO] Подсказка: %s %s\n", result, LocalDateTime.now());

        return result;
    }

    // проверяем наличие слова в истории ходов
    public boolean checkWordContainsInHistory(String guess) {
        for (GuessRecord guessRecord : guessHistory) {
            if (guessRecord.word.equals(guess)) {
                return true;
            }
        }
        return false;
    }

    // Проверка слова-кандидата:
    // 1. соответствует ли кандидат уже отгаданным позициям в слове '+'
    // 2. проверяем кандидата на отсутствие букв в позициях, где их точно быть не должно по символу '^'
    // 3. проверяем кандидата на список символов, которых точно не должно быть в слове '-'
    // 4. проверяем кандидата, что все символы из '^' и '+' присутствуют в слове
    public boolean checkCandidatesWordMatch(String guess) {
        // проверяем по спискам корректных и некорректных символов
        for (int i = 0; i < guess.length(); i++) {
            //проверяем соответствуют ли символы уже НАЙДЕННЫМ буквам
            if (fixedPositions.containsKey(i)) {
                if (guess.charAt(i) != fixedPositions.get(i)) {
                    return false;
                }
            }
            //проверяем отсутствие символов в списке букв forbiddenPositions
            if (forbiddenPositions.containsKey(i)) {
                Set<Character> forbiddenCharacters = forbiddenPositions.get(i);
                for (char c : forbiddenCharacters) {
                    if (c == guess.charAt(i)) {
                        return false;
                    }
                }
            }
            //проверка на некорректные символы
            if (isLetterInIncorrectList(guess.charAt(i))) {
                return false;
            }
        }

        // проверка на наличие всех корректных символов
        if (!isValidByCorrectLetters(guess)) {
            return false;
        }

        //если все проверки пройдены
        return true;
    }

    //проверка, что слово содержит все корректные буквы из списка correctLetters
    private boolean isValidByCorrectLetters(String guess) {
        for (Character correctLetter : correctLetters) {
            boolean letterFound = false;
            for (int i = 0; i < guess.length(); i++) {
                if (guess.charAt(i) == correctLetter) {
                    letterFound = true;
                    break;
                }
            }
            if (!letterFound) {
                return false;
            }
        }
        return true;
    }

    //проверка, что в слове есть совпадение с некорректными символами incorrectLetters
    private boolean isLetterInIncorrectList(char guess) {
        for (Character incorrectLetter : incorrectLetters) {
            if (guess == incorrectLetter) {
                return true;
            }
        }
        return false;
    }
}