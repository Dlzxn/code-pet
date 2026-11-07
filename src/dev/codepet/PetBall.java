package dev.codepet;

import java.awt.*;
import java.util.Random;

/** Viewport-local physics; all bounces stay within the current visible editor. */
final class PetBall {
    double x,y,vx,vy;long launched;boolean active;
    void launch(Rectangle area,Point at,long now,Random random){x=Math.max(area.x+8,Math.min(area.getMaxX()-8,at.x));y=Math.max(area.y+8,Math.min(area.getMaxY()-8,at.y));vx=(random.nextBoolean()?1:-1)*(160+random.nextInt(80));vy=-260;launched=now;active=true;}
    void update(Rectangle area,double seconds,long now){
        if(!active)return;if(area.width<20||area.height<20||now-launched>25_000_000_000L){active=false;return;}
        double dt=Math.max(0,Math.min(.06,seconds));vy+=430*dt;x+=vx*dt;y+=vy*dt;
        if(x<area.x+7){x=area.x+7;vx=Math.abs(vx)*.9;}if(x>area.getMaxX()-7){x=area.getMaxX()-7;vx=-Math.abs(vx)*.9;}
        if(y<area.y+7){y=area.y+7;vy=Math.abs(vy)*.83;}if(y>area.getMaxY()-7){y=area.getMaxY()-7;vy=-Math.abs(vy)*.79;vx*=.98;}
    }
    static double jump(String type){return switch(type){case "frog","rabbit"->450;case "squirrel","cat"->360;case "dragon","bat","owl","chick"->390;case "turtle","slime"->170;case "bear","panda","koala"->230;default->300;};}
}
