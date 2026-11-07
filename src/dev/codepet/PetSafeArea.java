package dev.codepet;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Returns an entire obstacle-free horizontal lane, or hides the pet. Coordinates are editor-local. */
final class PetSafeArea {
    static Rectangle lane(Rectangle viewport,Dimension sprite,List<Rectangle> obstacles){
        int left=viewport.x+12,right=viewport.x+viewport.width-12;
        for(int y=viewport.y+viewport.height-sprite.height-12;y>=Math.max(viewport.y+12,viewport.y+viewport.height-sprite.height-140);y-=28){
            ArrayList<int[]> intervals=new ArrayList<>();
            for(Rectangle r:obstacles)if(r.y<y+sprite.height&&r.y+r.height>y)intervals.add(new int[]{Math.max(left,r.x),Math.min(right,r.x+r.width)});
            intervals.sort(Comparator.comparingInt(a->a[0]));
            int start=left,bestStart=left,bestWidth=0;
            for(int[] blocked:intervals){if(blocked[1]<=start||blocked[0]>=right)continue;
                if(blocked[0]-start>bestWidth){bestStart=start;bestWidth=blocked[0]-start;}start=Math.max(start,blocked[1]);}
            if(right-start>bestWidth){bestStart=start;bestWidth=right-start;}
            if(bestWidth>=sprite.width)return new Rectangle(bestStart,y,bestWidth,sprite.height);
        }
        return null;
    }
}
