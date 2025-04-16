package com.zhsaidk.util;

public class SlugUtil {
    public static String toSlug(String input) {
        if (input == null) {
            return "";
        }
        String transliterated = input
                .replaceAll("[Аа]", "a")
                .replaceAll("[Бб]", "b")
                .replaceAll("[Вв]", "v")
                .replaceAll("[Гг]", "g")
                .replaceAll("[Дд]", "d")
                .replaceAll("[Ее]", "e")
                .replaceAll("[Ёё]", "yo")
                .replaceAll("[Жж]", "zh")
                .replaceAll("[Зз]", "z")
                .replaceAll("[Ии]", "i")
                .replaceAll("[Йй]", "y")
                .replaceAll("[Кк]", "k")
                .replaceAll("[Лл]", "l")
                .replaceAll("[Мм]", "m")
                .replaceAll("[Нн]", "n")
                .replaceAll("[Оо]", "o")
                .replaceAll("[Пп]", "p")
                .replaceAll("[Рр]", "r")
                .replaceAll("[Сс]", "s")
                .replaceAll("[Тт]", "t")
                .replaceAll("[Уу]", "u")
                .replaceAll("[Фф]", "f")
                .replaceAll("[Хх]", "kh")
                .replaceAll("[Цц]", "ts")
                .replaceAll("[Чч]", "ch")
                .replaceAll("[Шш]", "sh")
                .replaceAll("[Щщ]", "sch")
                .replaceAll("[Ъъ]", "")
                .replaceAll("[Ыы]", "y")
                .replaceAll("[Ьь]", "")
                .replaceAll("[Ээ]", "e")
                .replaceAll("[Юю]", "yu")
                .replaceAll("[Яя]", "ya");

        return transliterated
                .toLowerCase()                   // Convert to lowercase
                .replaceAll("[^a-z0-9\\s]", "") // Remove all except letters, digits, spaces
                .replaceAll("\\s+", "-")        // Replace spaces with hyphens
                .replaceAll("-{2,}", "-")       // Remove multiple hyphens
                .replaceAll("^-|-$", "");       // Remove leading/trailing hyphens
    }
}