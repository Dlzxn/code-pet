package dev.codepet;

import java.awt.event.KeyEvent;

/** Observes, never consumes editor keys. Repeat guards keep one held key from creating unlimited toys. */
final class PetPlayInput {
    long ballSerial,balloonSerial,lastTyping,lastMouse,deleteStart=-1,cinemaUntil,lastBall=-1,lastBalloon=-1;
    int deleteKey;
    java.awt.Point mouseScreen;
    PetPlayInput(long now){lastTyping=lastMouse=now;}
    void pressed(int code,long now,boolean games){
        lastTyping=now;if(!games)return;
        if(code==KeyEvent.VK_ENTER&&(lastBall<0||now-lastBall>=300_000_000L)){ballSerial++;lastBall=now;}
        if(code==KeyEvent.VK_TAB&&(lastBalloon<0||now-lastBalloon>=1_000_000_000L)){balloonSerial++;lastBalloon=now;}
        if((code==KeyEvent.VK_DELETE||code==KeyEvent.VK_BACK_SPACE)&&deleteStart<0){deleteStart=now;deleteKey=code;}
    }
    void released(int code,long now){if(code==deleteKey&&deleteStart>=0){if(now-deleteStart>=650_000_000L)cinemaUntil=now+4_000_000_000L;deleteStart=-1;}}
    boolean cinema(long now){return deleteStart>=0&&now-deleteStart>=650_000_000L||now<cinemaUntil;}
    boolean typing(long now){return now-lastTyping<4_000_000_000L;}
    boolean away(long now){return now-Math.max(lastMouse,lastTyping)>45_000_000_000L;}
    void reset(long now){lastMouse=lastTyping=now;deleteStart=-1;cinemaUntil=0;}
}
