package ru.yandex.practicum;

import ru.yandex.practicum.exceptions.GameConfigurationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */

public class WordleDictionaryLoader {

    private final PrintWriter log;

    WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary getWordleDictionary(String filename) throws GameConfigurationException {
        List<String> words = new ArrayList<>(70000);

        try (BufferedReader br = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))) {
            String word;
            while ((word = br.readLine()) != null) {
                if (WordleDictionary.isValidWord(word)) {
                    words.add(WordleDictionary.normalizeWord(word));
                }
            }
            log.println("[INFO] Сформировано " + words.size() + " слов для игры " + LocalDateTime.now());

            if (words.isEmpty()) {
                log.println("[ERROR] Невозможно продолжить игру, в файле отсутствуют подходящие слова " + LocalDateTime.now());
                throw new GameConfigurationException("Невозможно продолжить игру, в файле отсутствуют подходящие слова");
            }

            return new WordleDictionary(words);
        } catch (FileNotFoundException e) {
            log.printf("[ERROR] Файл словаря не найден: %s %s\n", filename, LocalDateTime.now());
            throw new GameConfigurationException("Файл словаря не найден: " + filename, e);
        } catch (IOException e) {
            log.printf("[ERROR] Ошибка чтения файла словаря: %s\n", e.getMessage());
            throw new GameConfigurationException("Ошибка чтения файла словаря: " + e.getMessage(), e);
        }
    }
}


