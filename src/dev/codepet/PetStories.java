package dev.codepet;

import java.util.*;
import static dev.codepet.PetBehavior.Action.*;

/** Twelve authored recipes per species. Shared chores have species-specific props and endings. */
final class PetStories {
    record Story(String name, PetBehavior.Action[] beats, String prop) {
        @Override public String toString(){return name;}
        double duration(){return Arrays.stream(beats).mapToDouble(a->a.duration).sum();}
    }
    private static final Map<String,String[]> THEMES=Map.ofEntries(
        Map.entry("cat",new String[]{"Клубок под скобкой","Лазерный зайчик","Охота на точку","Коробка с секретом","Усы в пене","Добыча на полку"}),
        Map.entry("dog",new String[]{"Косточка за полем","Вернуть мяч","След из запятых","Закопать сокровище","Поймать фрисби","Сторожить домик"}),
        Map.entry("fox",new String[]{"Лист за ухом","Хитрый тайник","Рыжая искорка","Следы в отступах","Письмо в норку","Замести хвостом следы"}),
        Map.entry("rabbit",new String[]{"Морковка из шляпы","Прыжки по точкам","Туннель в отступе","Уши ловят сигнал","Огород из скобок","Унести урожай"}),
        Map.entry("panda",new String[]{"Бамбуковая башня","Перекат через ветку","Палочки для пикника","Лист на носу","Тихая гимнастика","Положить бамбук на полку"}),
        Map.entry("bear",new String[]{"Горшочек мёда","Пчела над строкой","Пикник на пледе","Большой перекат","Ягоды в корзинку","Уютная берлога"}),
        Map.entry("koala",new String[]{"Ветка эвкалипта","Обнять скобку","Листовой зонтик","Смотреть с ветки","Листья в карман","Подушка из листьев"}),
        Map.entry("raccoon",new String[]{"Вымыть блестяшку","Тайник из скобок","Полосатый сыщик","Находка под коробкой","Полировать монетку","Унести в рюкзак"}),
        Map.entry("hamster",new String[]{"Зерно за щекой","Колесо из скобок","Хлебная крошка","Щёки полны находок","Лабиринт из кубиков","Запасы в домик"}),
        Map.entry("mouse",new String[]{"Сырный лабиринт","Норка под точкой","Крошка на верёвке","Следовать запаху","Сырная башня","Маленькая кладовая"}),
        Map.entry("squirrel",new String[]{"Орех в тайнике","Прыжок с ветки","Хвост-парашют","Шишка из кармана","Ореховый перекат","Пересчитать запасы"}),
        Map.entry("hedgehog",new String[]{"Яблоко на иголках","Свернуться в клубок","Грибной маршрут","Лист на спине","Покатить каштан","Подстилка из листьев"}),
        Map.entry("owl",new String[]{"Письмо на крыльях","Ночной дозор","Перо между строк","Звёздная карта","Взмах над скобкой","Книга в гнездо"}),
        Map.entry("penguin",new String[]{"Ледяная горка","Рыбка на льдине","Скольжение по строке","Снежок из точки","Шарф на ветру","Льдинка у домика"}),
        Map.entry("chick",new String[]{"Скорлупа-сюрприз","Крошки по следу","Первый полёт","Зерно под буквой","Пёрышко на ветру","Гнездо из скобок"}),
        Map.entry("dragon",new String[]{"Искорка вместо огня","Сокровище под крылом","Полёт над строкой","Золотая чешуйка","Согреть чашку","Унести монету в логово"}),
        Map.entry("dino",new String[]{"Ископаемая скобка","Следы великана","Листовой перекус","Окаменелая точка","Забавный рёв","Музей в домике"}),
        Map.entry("turtle",new String[]{"Панцирь-домик","Гонка с запятой","Листовой кораблик","Спрятаться целиком","Медленный мостик","Полка в панцире"}),
        Map.entry("frog",new String[]{"Лилия под строкой","Поймать светлячка","Пузырь из точки","Прыжок через пруд","Песня в кружке","Лилия у порога"}),
        Map.entry("axolotl",new String[]{"Пузырьковый дождь","Водоросль-шарф","Жабры-веер","Отыскать жемчужину","Поплавать в скобках","Аквариумный сад"}),
        Map.entry("bat",new String[]{"Повиснуть на скобке","Эхо между строк","Крылья-плед","Ночной вираж","Светлячок в пещере","Перевернуть домик"}),
        Map.entry("ghost",new String[]{"Остались только глаза","Пройти сквозь скобку","Собраться из тумана","Призрачный фонарик","Левитирующая чашка","Вернуться в картину"}),
        Map.entry("robot",new String[]{"Зарядиться от точки","Починить шестерёнку","Прочитать сигнал","Магнит для скобок","Танец сервоприводов","Поставить деталь на полку"}),
        Map.entry("slime",new String[]{"Пузырёк внутри","Стать капелькой","Желейный мостик","Поглотить блёстку","Пружинка из желе","Устроиться в баночке"})
    );
    static List<Story> forType(String type){
        String[] names=THEMES.getOrDefault(type,THEMES.get("cat"));ArrayList<Story> stories=new ArrayList<>();
        stories.add(new Story("Подмести дорожку",new PetBehavior.Action[]{FETCH_BROOM,SWEEP,DUST_LETTERS,PUT_AWAY},"broom"));
        stories.add(new Story("Помыть буквы",new PetBehavior.Action[]{FETCH_BUCKET,WASH_LETTERS,POLISH_LETTERS,PUT_AWAY},"bucket"));
        stories.add(new Story("Потерянная игрушка",new PetBehavior.Action[]{EMERGE_BOX,PLAY_BALL,SEARCH_TOY,FIND_TOY,RETURN_HOME},"ball"));
        stories.add(new Story("Пикник с уборкой",new PetBehavior.Action[]{PICNIC,EAT,WASH_LETTERS,PACK_PICNIC,RETURN_HOME},"basket"));
        stories.add(new Story("Мастерская скобок",new PetBehavior.Action[]{LETTER_LIFT,BUILD_BLOCKS,REPAIR,SHOW_FIND,RETURN_HOME},"bracket"));
        stories.add(new Story("Вещи на место",new PetBehavior.Action[]{TRY_CAPE,POLISH_GLASSES,PUT_AWAY,RETURN_HOME},"wardrobe"));
        PetBehavior.Action[][] beats={
            {WATCH,SPECIES_TRICK,SPECIES_TRICK,SHOW_FIND},
            {SNIFF,SPECIES_TRICK,PLAY_BALL,RETURN_HOME},
            {LETTER_PEEK,SPECIES_TRICK,LETTER_BRIDGE,SPECIES_TRICK},
            {SEARCH_TOY,SPECIES_TRICK,FIND_TOY,SHOW_FIND},
            {STRETCH,SPECIES_TRICK,DRINK,SPECIES_TRICK},
            {SHOW_FIND,SPECIES_TRICK,PUT_AWAY,RETURN_HOME}};
        for(int i=0;i<6;i++)stories.add(new Story(names[i],beats[i],"species-"+i));
        return List.copyOf(stories);
    }
    static Story choose(PetModel pet,Random random,boolean away){
        List<Story> choices=forType(pet.type);
        int index;
        if(away&&random.nextDouble()<.55)index=random.nextInt(2);
        else if("Спокойный".equals(pet.temperament))index=new int[]{1,3,5,6,10,11}[random.nextInt(6)];
        else if("Энергичный".equals(pet.temperament))index=new int[]{2,4,6,7,8,9}[random.nextInt(6)];
        else index=random.nextInt(choices.size());
        return choices.get(index);
    }
}
