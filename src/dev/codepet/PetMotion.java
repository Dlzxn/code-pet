package dev.codepet;

/** Critically damped movement. Stable frame times and fractional coordinates prevent pixel hopping. */
final class PetMotion {
    private double x,y,vx,vy;private boolean initialized;
    void reset(double x,double y){this.x=x;this.y=y;vx=vy=0;initialized=true;}
    java.awt.Point approach(double tx,double ty,double seconds){
        if(!initialized)reset(tx,ty);
        double dt=Math.max(0,Math.min(.08,seconds)),omega=12,decay=Math.exp(-omega*dt);
        double dx=x-tx,dy=y-ty,ax=vx+omega*dx,ay=vy+omega*dy;
        x=tx+(dx+ax*dt)*decay;y=ty+(dy+ay*dt)*decay;
        vx=(vx-omega*ax*dt)*decay;vy=(vy-omega*ay*dt)*decay;
        return new java.awt.Point((int)Math.round(x),(int)Math.round(y));
    }
    static double jumpHeight(double phase){double cycle=(phase%(Math.PI*2))/(Math.PI*2);return cycle<.18||cycle>.84?0:22*Math.sin((cycle-.18)/.66*Math.PI);}
    static double jumpSquash(double phase){double cycle=(phase%(Math.PI*2))/(Math.PI*2);return cycle<.18?Math.sin(cycle/.18*Math.PI)*.13:cycle>.84?Math.sin((cycle-.84)/.16*Math.PI)*.1:0;}
    void clear(){initialized=false;vx=vy=0;}
}
