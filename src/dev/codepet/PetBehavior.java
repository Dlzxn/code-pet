package dev.codepet;

import java.util.Random;

/** 36 small scenes, shared by every species and adapted by its anatomy in the renderer. No XP access. */
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
        TELEPORT("Телепортируется",2), VANISH("Растворяется в искрах",2.5), HIDDEN("Исследует закулисье",4), SNIFF("Нюхает цветок",4);
        final String label; final double duration;
        Action(String label,double duration){this.label=label;this.duration=duration;}
        boolean moves(){return this==WALK||this==RUN||this==HOP||this==TIPTOE||this==BACKSTEP||this==CHASE_SPARK;}
        boolean exits(){return this==HIDE_BOX||this==FALL_HOLE||this==TELEPORT||this==VANISH;}
    }
    record Frame(Action action,double phase,double progress,int direction,boolean hovered){}
    private final Random random;
    private Action action=Action.APPEAR_PORTAL;
    private double elapsed,phase,position=.5;
    private int direction=1;
    private boolean hovered;
    private String temperament="Любопытный";
    PetBehavior(){this(new Random());}
    PetBehavior(Random random){this.random=random;}
    Action action(){return action;} double phase(){return phase;} double position(){return position;} int direction(){return direction;}
    Frame frame(){return new Frame(action,phase,Math.min(1,elapsed/action.duration),direction,hovered);}
    void hover(boolean value){hovered=value;}
    void temperament(String value){temperament=value;}
    void play(){force(Action.PLAY_BALL);}
    void eat(){force(Action.EAT);}
    void force(Action next){action=next;elapsed=0;}
    void update(double seconds,double travelWidth,boolean animated){
        if(!animated||hovered)return;
        seconds=Math.max(0,Math.min(.1,seconds));
        phase+=seconds*(action==Action.RUN?12:6);elapsed+=seconds;
        if(elapsed>=action.duration){
            if(action.exits())force(Action.HIDDEN);
            else if(action==Action.HIDDEN){position=.1+random.nextDouble()*.8;force(Action.values()[random.nextInt(4)]);}
            else if(action.ordinal()<4)force(Action.WALK);
            else {
                Action next;
                do{next=Action.values()[4+random.nextInt(Action.values().length-4)];}while(next==action||next==Action.HIDDEN||next==Action.EAT);
                if("Спокойный".equals(temperament)&&next==Action.RUN)next=Action.READ;
                force(next);if(random.nextBoolean())direction=-direction;
            }
        }
        if(action.moves()){
            double speed=action==Action.RUN?48:action==Action.TIPTOE?9:20;
            if("Энергичный".equals(temperament))speed*=1.3;
            position+=direction*seconds*speed/Math.max(1,travelWidth);
            if(position>=1){position=1;direction=-1;}if(position<=0){position=0;direction=1;}
        }
    }
}
