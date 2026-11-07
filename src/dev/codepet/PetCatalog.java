package dev.codepet;

import java.awt.Color;
import java.util.*;

final class PetCatalog {
    record Species(String id, String name, String shape, String detail) {
        @Override public String toString() { return name; }
    }
    static final Species[] SPECIES = {
        new Species("cat", "Кот", "cat", "whiskers"), new Species("dog", "Пёс", "dog", "muzzle"),
        new Species("fox", "Лис", "cat", "fox"), new Species("rabbit", "Кролик", "rabbit", "teeth"),
        new Species("panda", "Панда", "round", "panda"), new Species("bear", "Медведь", "round", "muzzle"),
        new Species("koala", "Коала", "round", "koala"), new Species("raccoon", "Енот", "cat", "mask"),
        new Species("hamster", "Хомяк", "round", "cheeks"), new Species("mouse", "Мышь", "round", "mouse"),
        new Species("squirrel", "Белка", "cat", "squirrel"), new Species("hedgehog", "Ёж", "round", "spikes"),
        new Species("owl", "Сова", "bird", "owl"), new Species("penguin", "Пингвин", "bird", "penguin"),
        new Species("chick", "Цыплёнок", "bird", "chick"), new Species("dragon", "Дракон", "reptile", "wings"),
        new Species("dino", "Динозавр", "reptile", "spikes"), new Species("turtle", "Черепаха", "reptile", "shell"),
        new Species("frog", "Лягушка", "frog", "spots"), new Species("axolotl", "Аксолотль", "frog", "gills"),
        new Species("bat", "Летучая мышь", "cat", "bat"), new Species("ghost", "Привидение", "ghost", "ghost"),
        new Species("robot", "Робот", "robot", "antenna"), new Species("slime", "Слайм", "slime", "shine")
    };
    static final String[] COLORS = {"Мятный", "Персиковый", "Лавандовый", "Небесный", "Коралловый", "Лимонный", "Розовый", "Бирюзовый", "Сливовый", "Песочный", "Серебряный", "Снежный", "Графитовый", "Изумрудный", "Карамельный", "Индиго"};
    private static final int[] RGB = {0x8DD8BC,0xEDB18F,0xB7A2E4,0x8CBFEA,0xEC8E87,0xE9D482,0xE7AED0,0x7BCBCB,0xA486B5,0xD7C6A1,0xB8C1CC,0xE8EBE9,0x647181,0x6DB391,0xC7946C,0x8995D4};
    static final String[] PATTERNS = {"Без узора", "Пятна", "Полоски", "Веснушки", "Звёздочки", "Носочки"};
    static final String[] HEADS = {"Без головного убора", "Шапка", "Берет", "Корона", "Колпак мага", "Цилиндр", "Цветок", "Наушники", "Нимб", "Рога"};
    static final String[] FACES = {"Без очков", "Круглые очки", "Солнечные очки", "Монокль", "Маска героя", "Визор"};
    static final String[] NECKS = {"Без украшения", "Шарф", "Бант", "Медальон", "Галстук", "Бабочка", "Бусы"};
    static final String[] BACKS = {"Без плаща", "Плащ героя", "Плащ мага", "Рюкзак", "Крылья феи", "Крылья ангела", "Джетпак"};
    static final String[] EYES = {"Круглые", "Большие", "Сонные", "Звёздные"};
    static final String[] TAILS = {"Обычный", "Пушистый", "Короткий", "Без хвоста"};
    static final String[] TEMPERAMENTS = {"Любопытный", "Спокойный", "Энергичный"};
    static Species species(String id) { return Arrays.stream(SPECIES).filter(s -> s.id.equals(id)).findFirst().orElse(SPECIES[0]); }
    static Color color(String name) { for (int i=0;i<COLORS.length;i++) if(COLORS[i].equals(name)) return new Color(RGB[i]); return new Color(RGB[0]); }
    static String valid(String value, String[] options) { return Arrays.asList(options).contains(value) ? value : options[0]; }
}
