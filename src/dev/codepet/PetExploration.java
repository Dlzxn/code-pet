package dev.codepet;

import java.util.Random;

/** Mouse idle permits exploration; keyboard idle distinguishes absence from active typing. */
final class PetExploration {
    static final long IDLE_NANOS=10_000_000_000L, AWAY_NANOS=45_000_000_000L;
    enum Mode { CLASSIC,CAMEO,EXPLORE }
    enum Place { TOP,TREE,LETTER,WINDOW }
    private final Random random;
    private long lastMouse,lastTyping,nextCameo,started,until,nextVisit,episode,now,duration=35_000_000_000L;
    private Mode mode=Mode.CLASSIC;private Place place=Place.TOP;
    private String intensity="Живой";private boolean enlarged;
    PetExploration(long now){this(now,new Random());}
    PetExploration(long now,Random random){this.random=random;lastMouse=lastTyping=this.now=now;nextCameo=now+90_000_000_000L;}
    void intensity(String value){if(!intensity.equals(value)){intensity=value;nextCameo=now+interval();}}
    boolean typing(long time){lastTyping=time;if(mode==Mode.EXPLORE){reset(time);return true;}return false;}
    boolean mouseActivity(long time){lastMouse=time;boolean changed=mode!=Mode.CLASSIC;if(changed)reset(time);return changed;}
    void reset(long time){now=time;mode=Mode.CLASSIC;started=until=nextVisit=0;enlarged=false;nextCameo=time+interval();}
    private long interval(){return ("Спокойный".equals(intensity)?150L:"Хаос".equals(intensity)?60L:100L)*1_000_000_000L+random.nextInt(40)*1_000_000_000L;}
    void pause(long time){lastMouse=lastTyping=time;reset(time);}
    boolean typingRecently(){return now-lastTyping<4_000_000_000L;}
    boolean away(){return now-Math.max(lastMouse,lastTyping)>=AWAY_NANOS;}
    void extend(double seconds){duration=(long)(Math.max(1,seconds)*1e9);if(mode!=Mode.EXPLORE){until=started+duration;nextVisit=until+3_000_000_000L;}}
    void completeStory(long time){if(mode==Mode.EXPLORE&&until==Long.MAX_VALUE){until=time+600_000_000L;nextVisit=until+3_000_000_000L;}}
    void place(Place value){place=value;}
    void update(long time,boolean animated,boolean exploration,boolean cameos){
        now=time;
        if(!animated){pause(time);return;}
        // No adventurous code visits while typing, even if the mouse is stationary.
        if(exploration&&time-lastMouse>=IDLE_NANOS&&!typingRecently()){
            if(mode!=Mode.EXPLORE){mode=Mode.EXPLORE;begin(time,true);}
            else if(time>=nextVisit)begin(time,true);
            return;
        }
        if(mode==Mode.EXPLORE)reset(time);
        if(mode==Mode.CAMEO&&(!cameos||time>=until))reset(time);
        if(mode==Mode.CLASSIC&&cameos&&time>=nextCameo){mode=Mode.CAMEO;begin(time,false);}
    }
    private void begin(long time,boolean exploring){
        episode++;started=time;
        duration=exploring?35_000_000_000L:5_000_000_000L;until=exploring?Long.MAX_VALUE:time+duration;nextVisit=exploring?Long.MAX_VALUE:until+3_000_000_000L;
        if(!exploring||typingRecently())place=Place.TOP;
        else place=Place.values()[random.nextInt(Place.values().length)];
        enlarged=exploring&&random.nextDouble()<.28;
    }
    Mode mode(){return mode;}Place place(){return place;}long episode(){return episode;}
    boolean enlarged(){return enlarged&&mode==Mode.EXPLORE;}
    boolean visiting(){return visitingAt(now);}boolean visitingAt(long time){return mode!=Mode.CLASSIC&&time>=started&&time<until;}
    double progress(){return Math.max(0,Math.min(1,(now-started)/(double)duration));}
    float alpha(){return (float)Math.max(0,Math.min(1,Math.min((now-started)/450_000_000.0,(until-now)/600_000_000.0)));}
}
