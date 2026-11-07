package dev.codepet;

import java.util.Random;

/** 50 small scenes, shared by every species and adapted by its anatomy in the renderer. No XP access. */
final class PetBehavior {
    enum Action {
        APPEAR_PORTAL("Появляется из портала",2.4), EMERGE_BOX("Вылезает из коробки",2.8), PEEK_EDGE("Выглядывает",2.5), DROP_IN("Спускается на зонтике",3),
        WALK("Гуляет",5), RUN("Бежит по делам",3), HOP("Двигается вприпрыжку",3), TIPTOE("Крадётся",4), BACKSTEP("Пятится",3),
        SIT("Отдыхает",6), WATCH("Следит за искоркой",5), BLINK("Медленно жмурится",3), STRETCH("Потягивается",4),
        GROOM("Умывается",5), SHAKE("Отряхивается",2), SPIN("Кружится",2.5), ROLL("Кувыркается",2.8), DANCE("Танцует",4),
        TAIL_CHASE("Ловит свой хвост",4), PLAY_BALL("Играет с мячом",5), CHASE_SPARK("Ловит светлячка",5),
        READ("Читает книжку",6), TYPE("Печатает на ноутбуке",5), EAT("Перекусывает",5), DRINK("Пьёт чай",5),
        YAWN("Зевает",3), WAVE("Машет лапой",3), HEART("Посылает сердечко",3), STAR_GAZE("Смотрит на звёзды",5),
        FLOAT("Парит",4), HIDE_BOX("Прячется в коробке",2.8), FALL_HOLE("Проваливается в норку",2.5),
        TELEPORT("Телепортируется",2), VANISH("Растворяется в искрах",2.5), HIDDEN("Исследует закулисье",4), SNIFF("Нюхает цветок",4),
        LETTER_PEEK("Выглядывает из-под буквы",2.6), LETTER_LIFT("Поднимает букву",2.6),
        LETTER_SLIDE("Катается на букве",2.6), BRACKET_SWING("Качается на скобках",2.6), LETTER_BRIDGE("Прыгает по символам",2.6),
        SURPRISE("Выскочил: бу!",2.6), RIDE_PENCIL("Едет на карандаше",4), BUILD_BLOCKS("Строит башню",5),
        CATCH_FISH("Ловит игрушечную рыбу",5), PICNIC("Устроил пикник",5), TOY_TRAIN("Запускает поезд",5),
        PAPER_PLANE("Запускает самолётик",4), TRY_CAPE("Примеряет свой плащ",4), POLISH_GLASSES("Протирает очки",4),
        FETCH_BROOM("Достаёт метлу",3), SWEEP("Подметает пиксели",9), DUST_LETTERS("Смахивает пыль с букв",8),
        FETCH_BUCKET("Несёт ведёрко",3), WASH_LETTERS("Моет буквы",9), POLISH_LETTERS("Полирует символы",7),
        PUT_AWAY("Убирает вещи на полку",4), SEARCH_TOY("Ищет потерянную игрушку",5), FIND_TOY("Нашёл игрушку!",4),
        RETURN_HOME("Возвращается в уголок",5), PACK_PICNIC("Складывает плед",4), REPAIR("Мастерит находку",6),
        SHOW_FIND("Показывает находку",4), SHOW_GIFT("Открывает подарок за уровень",6),
        SPECIES_TRICK("Занимается любимым делом",7), CELEBRATE("Радуется успешным тестам",4), THINK("Спокойно поддерживает",4), BALL_THINK("Оценивает траекторию мяча",.65), BALLOON_RIDE("Улетает с шариком",14), CINEMA("Смотрит из кресла с попкорном",20);
        final String label; final double duration;
        Action(String label,double duration){this.label=label;this.duration=duration;}
        boolean moves(){return this==WALK||this==RUN||this==HOP||this==TIPTOE||this==BACKSTEP||this==CHASE_SPARK||this==RIDE_PENCIL||this==LETTER_SLIDE||this==LETTER_BRIDGE||this==SWEEP||this==DUST_LETTERS||this==SEARCH_TOY||this==RETURN_HOME;}
        boolean exits(){return this==HIDE_BOX||this==FALL_HOLE||this==TELEPORT||this==VANISH;}
    }
    record Frame(Action action,double phase,double progress,int direction,boolean hovered,char glyph,String prop){
        Frame(Action action,double phase,double progress,int direction,boolean hovered){this(action,phase,progress,direction,hovered,';',"");}
        Frame(Action action,double phase,double progress,int direction,boolean hovered,char glyph){this(action,phase,progress,direction,hovered,glyph,"");}
    }
    private final Random random;
    private Action action=Action.APPEAR_PORTAL;
    private double elapsed,phase,position=.5,velocity;
    private int direction=1,step,completed;
    private boolean hovered,away;
    private char glyph=';';
    private String temperament="Любопытный";
    private PetModel profile;
    private PetStories.Story story;
    PetBehavior(){this(new Random());}
    PetBehavior(Random random){this.random=random;}
    Action action(){return action;} double phase(){return phase;} double position(){return position;} int direction(){return direction;}
    Frame frame(){return new Frame(action,phase,Math.min(1,elapsed/action.duration),direction,hovered,glyph,story==null?"":story.prop());}
    String storyLabel(){return story==null?action.label:story.name()+" · "+(step+1)+"/"+story.beats().length;}
    boolean inStory(){return story!=null;}
    double storyDuration(){return story==null?action.duration:story.duration();}
    int consumeCompleted(){int count=completed;completed=0;return count;}
    void hover(boolean value){hovered=value;}
    void temperament(String value){temperament=value;}
    void autonomy(PetModel pet,boolean away){profile=pet;temperament=pet.temperament;this.away=away;}
    void play(){startStory(new PetStories.Story("Потерянный мяч",new Action[]{Action.PLAY_BALL,Action.SEARCH_TOY,Action.FIND_TOY,Action.RETURN_HOME},"ball"));}
    void glyph(char value){glyph=value;}
    void letters(int choice){force(new Action[]{Action.LETTER_PEEK,Action.LETTER_LIFT,Action.LETTER_SLIDE,Action.BRACKET_SWING,Action.LETTER_BRIDGE}[Math.floorMod(choice,5)]);}
    void eat(){force(Action.EAT);}
    void force(Action next){story=null;setAction(next);}
    private void setAction(Action next){action=next;elapsed=0;}
    void startStory(PetStories.Story value){story=value;step=0;setAction(value.beats()[0]);}
    void update(double seconds,double travelWidth,boolean animated){
        if(!animated||hovered)return;
        seconds=Math.max(0,Math.min(.1,seconds));
        phase+=seconds*(action==Action.RUN?12:6);elapsed+=seconds;
        if(elapsed>=action.duration){
            if(story!=null){
                step++;
                if(step<story.beats().length)setAction(story.beats()[step]);
                else {story=null;completed++;setAction(Action.SIT);}
            }else if(action.exits())setAction(Action.HIDDEN);
            else if(action==Action.HIDDEN){position=.1+random.nextDouble()*.8;setAction(Action.values()[random.nextInt(4)]);}
            else if(action.ordinal()<4)setAction(Action.WALK);
            else if(profile!=null&&random.nextDouble()<.72)startStory(PetStories.choose(profile,random,away));
            else {
                Action next;
                do{next=Action.values()[4+random.nextInt(50-4)];}while(next==action||next==Action.HIDDEN);
                if("Спокойный".equals(temperament)&&next==Action.RUN)next=Action.READ;
                setAction(next);if(random.nextBoolean())direction=-direction;
            }
        }
        double speed=action.moves()?(action==Action.RUN?48:action==Action.TIPTOE?9:20):0;
        if("Энергичный".equals(temperament))speed*=1.3;
        velocity+=(direction*speed-velocity)*(1-Math.exp(-seconds*7));
        position+=velocity*seconds/Math.max(1,travelWidth);
        if(position>=1){position=1;direction=-1;velocity=0;}if(position<=0){position=0;direction=1;velocity=0;}
    }
}
