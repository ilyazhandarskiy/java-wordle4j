package ru.yandex.practicum;

import ru.yandex.practicum.exceptions.GameConfigurationException;
import ru.yandex.practicum.exceptions.WordNotFoundInDictionary;
import ru.yandex.practicum.exceptions.WordleGameException;
import ru.yandex.practicum.exceptions.WrongInputWordExeption;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public class Wordle {

    private static final String WORDS_FILE_NAME = "words_ru.txt";
    private static final String LOG_FILE_NAME = "log.txt";
    private static final int MAX_ATTEMPTS_IN_GAME = 6;


    public static void main(String[] args) {

        try (PrintWriter log = new PrintWriter(new FileOutputStream(LOG_FILE_NAME), true)) {
            //создаем лог файл
            log.println("[INFO] Создан файл лога " + LocalDateTime.now());

            //создаем загрузчик словаря из файла
            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

            //получаем итоговый словарь из загрузчика словаря
            WordleDictionary dictionary = loader.getWordleDictionary(WORDS_FILE_NAME);

            //создаем игровой процесс
            WordleGame game = new WordleGame(dictionary, MAX_ATTEMPTS_IN_GAME, log);

            // печатаем меню
            printMenu();

            //создаем сканер для ввода пользователя
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

            while (true) {
                String userInput = scanner.nextLine();

                //Обработка подсказок
                if (userInput.isBlank()) {
                    try {
                        userInput = game.getHintWord();
                    } catch (WordleGameException e) {
                        System.out.println(e.getMessage());
                        System.out.println(" Попробуйте еще раз.");
                        continue;
                    }
                    System.out.println(userInput);
                }

                String hint;

                try {
                    hint = game.makeGuess(userInput);
                } catch (WordNotFoundInDictionary | WrongInputWordExeption e) {
                    System.out.print(e.getMessage());
                    System.out.println(" Попробуйте еще раз.");
                    continue;
                }
                //выводим подсказку
                System.out.print(hint);

                if (game.isWon()) {
                    System.out.printf("\nВы угадали! Было загадано слово: \"%s\"\n", game.getAnswer());
                    break;
                }

                if (game.isGameOver()) {
                    System.out.printf("\nВы проиграли... Было загадано слово: \"%s\"\n", game.getAnswer());
                    break;
                }
                System.out.printf("   Осталось попыток: %s\n", game.getRemainingSteps());
            }
        } catch (GameConfigurationException e) {
            System.err.println("Критическая ошибка при загрузке игры. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("Добро пожаловать в Wordle!");
        System.out.println("Правила:");
        System.out.println("  + — буква на правильном месте");
        System.out.println("  ^ — буква есть в слове, но не на этом месте");
        System.out.println("  - — такой буквы нет в слове");
        System.out.printf("У вас %s попыток.\n", MAX_ATTEMPTS_IN_GAME);
        System.out.println("Введите слово или нажмите Enter для получения подсказки:");
    }
}
