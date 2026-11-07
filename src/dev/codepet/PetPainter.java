package dev.codepet;

import java.awt.*;
import java.awt.geom.*;

/** Vector characters: distinct anatomy, composable wardrobe, and scene-specific transforms/props. */
final class PetPainter {
    private static final Color INK=new Color(43,53,69),PINK=new Color(231,154,173);
    static void paint(Graphics2D original,int width,int height,PetModel pet,PetBehavior.Frame frame,String speech,double scale){
        Graphics2D g=(Graphics2D)original.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(width/2.0,height-16);g.scale(scale,scale);
            PetBehavior.Action a=frame.action();double p=frame.progress(),t=frame.phase();
            if(a==PetBehavior.Action.HIDDEN)return;
            Color accent=PetCatalog.color(pet.accessoryColor);
            g.setColor(new Color(30,40,65,28));oval(g,-51,-3,102,11);
            boolean portal=a==PetBehavior.Action.APPEAR_PORTAL||a==PetBehavior.Action.TELEPORT;
            boolean hole=a==PetBehavior.Action.FALL_HOLE;
            if(portal||hole){g.setColor(hole?new Color(53,49,70):accent);oval(g,-58,-10,116,20);g.setColor(hole?new Color(20,22,35):accent.brighter());g.setStroke(new BasicStroke(3));g.draw(new Ellipse2D.Double(-58,-10,116,20));}
            Graphics2D body=(Graphics2D)g.create();
            try{
                double ease=p*p*(3-2*p),alpha=1;
                if(a==PetBehavior.Action.APPEAR_PORTAL){body.translate(0,6);body.scale(Math.max(.01,ease),Math.max(.01,ease));alpha=ease;}
                if(a==PetBehavior.Action.VANISH||a==PetBehavior.Action.TELEPORT){alpha=1-ease;body.scale(Math.max(.01,1-ease),Math.max(.01,1-ease));}
                if(a==PetBehavior.Action.EMERGE_BOX||a==PetBehavior.Action.PEEK_EDGE){body.clip(new Rectangle2D.Double(-130,-250,260,250));body.translate(0,150*(1-ease));}
                if(a==PetBehavior.Action.HIDE_BOX||hole){body.clip(new Rectangle2D.Double(-130,-250,260,250));body.translate(0,180*ease);}
                if(a==PetBehavior.Action.DROP_IN)body.translate(0,-160*(1-ease));
                body.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)Math.max(0,Math.min(1,alpha))));
                if(pet.animated){
                    if(a.moves())body.translate(0,Math.sin(t*2)*(a==PetBehavior.Action.TIPTOE?1:3));
                    if(a==PetBehavior.Action.HOP)body.translate(0,-Math.abs(Math.sin(t))*18);
                    if(a==PetBehavior.Action.FLOAT)body.translate(0,-12-Math.sin(t)*9);
                    if(a==PetBehavior.Action.SHAKE)body.translate(Math.sin(t*7)*6,0);
                    if(a==PetBehavior.Action.SPIN||a==PetBehavior.Action.TAIL_CHASE)body.scale(Math.cos(p*Math.PI*4)*.75+.25,1);
                    if(a==PetBehavior.Action.ROLL){body.translate(0,-45);body.rotate(p*Math.PI*2);body.translate(0,45);}
                    if(a==PetBehavior.Action.DANCE){body.rotate(Math.sin(t*2)*.14,0,-45);body.translate(0,-Math.abs(Math.sin(t*2))*5);}
                    if(a==PetBehavior.Action.STRETCH){double stretch=Math.sin(p*Math.PI);body.scale(1+.22*stretch,1-.18*stretch);}
                }
                body.scale(a==PetBehavior.Action.BACKSTEP?-frame.direction():frame.direction(),1);
                character(body,pet,frame);
            }finally{body.dispose();}
            props(g,pet,frame,accent);
            if(!speech.isBlank())bubble(original,width,height,speech,scale);
        }finally{g.dispose();}
    }
    private static void character(Graphics2D g,PetModel pet,PetBehavior.Frame f){
        PetCatalog.Species species=PetCatalog.species(pet.type);
        String shape=species.shape(),detail=species.detail();
        Color fur=PetCatalog.color(pet.color),secondary=PetCatalog.color(pet.secondaryColor),accent=PetCatalog.color(pet.accessoryColor);
        double t=f.phase();PetBehavior.Action a=f.action();
        boolean bird=shape.equals("bird"),robot=shape.equals("robot"),ghost=shape.equals("ghost"),slime=shape.equals("slime");
        back(g,pet.back,accent,t);
        if(detail.equals("wings")||detail.equals("bat")){g.setColor(fur.darker());for(int side:new int[]{-1,1}){Path2D wing=new Path2D.Double();wing.moveTo(side*25,-45);wing.lineTo(side*95,-108);wing.lineTo(side*78,-37);wing.lineTo(side*55,-52);wing.lineTo(side*40,-21);wing.closePath();g.fill(wing);}}
        if(detail.equals("spikes")){g.setColor(fur.darker());for(int i=0;i<9;i++){
            double angle=Math.PI+i*Math.PI/8;double x=Math.cos(angle)*49,y=-48+Math.sin(angle)*48;
            polygon(g,x-10,y+10,x,y-20,x+12,y+10);
        }}
        if(!bird&&!ghost&&!robot&&!slime&&!pet.tail.equals("Без хвоста")){
            g.setColor(fur.darker());
            if(detail.equals("squirrel")){oval(g,21,-90,64,90);g.setColor(secondary);oval(g,40,-79,29,55);}
            else {float stroke=pet.tail.equals("Пушистый")?24:detail.equals("mouse")?5:11;
                g.setStroke(new BasicStroke(stroke,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                double wave=pet.animated?Math.sin(t)*7:0;
                g.draw(new CubicCurve2D.Double(28,-23,pet.tail.equals("Короткий")?55:85,-12,65+wave,-78,52,-62));
                if(detail.equals("mask")){g.setColor(secondary);g.draw(new Line2D.Double(61,-30,69,-37));}
            }
        }
        g.setColor(fur);
        if(robot)round(g,-35,-72,70,71,15);
        else if(ghost){Path2D b=new Path2D.Double();b.moveTo(-43,0);b.curveTo(-53,-150,53,-150,43,0);for(int i=0;i<4;i++)b.lineTo(43-i*24,((i&1)==0?-12:0));b.closePath();g.fill(b);}
        else if(slime){Path2D b=new Path2D.Double();b.moveTo(-53,0);b.curveTo(-83,-35,-11,-118,9,-93);b.curveTo(25,-71,73,-32,53,0);b.closePath();g.fill(b);}
        else oval(g,-37,-75,74,76);
        if(!ghost&&!slime){g.setColor(secondary);oval(g,-22,-57,44,44);}
        if(detail.equals("shell")){g.setColor(fur.darker());oval(g,-39,-67,78,64);g.setColor(secondary);g.setStroke(new BasicStroke(3));g.draw(new Ellipse2D.Double(-29,-59,58,47));g.drawLine(0,-58,0,-13);g.drawLine(-28,-35,28,-35);}
        double step=pet.animated&&a.moves()?Math.sin(t*2)*7:0;
        if(!ghost&&!slime){g.setColor(bird?new Color(227,172,91):fur);round(g,-35+step,-13,29,18,11);round(g,7-step,-13,29,18,11);}
        if(!ghost&&!slime){
            g.setColor(fur);
            switch(shape){
                case "cat" -> {polygon(g,-43,-94,-42,-145,-9,-113);polygon(g,43,-94,42,-145,9,-113);g.setColor(PINK);polygon(g,-35,-113,-35,-131,-23,-117);polygon(g,35,-113,35,-131,23,-117);}
                case "dog" -> {oval(g,-59,-123,30,65);oval(g,29,-123,30,65);}
                case "rabbit" -> {oval(g,-34,-178,23,83);oval(g,11,-178,23,83);g.setColor(PINK);oval(g,-28,-165,10,53);oval(g,17,-165,10,53);}
                case "round" -> {double ear=detail.equals("koala")||detail.equals("mouse")?42:28;oval(g,-47-ear/2,-127,ear,ear);oval(g,47-ear/2,-127,ear,ear);g.setColor(secondary);oval(g,-42-ear/2,-122,ear-10,ear-10);oval(g,52-ear/2,-122,ear-10,ear-10);}
                case "frog" -> {oval(g,-44,-133,34,35);oval(g,10,-133,34,35);}
                case "reptile" -> {polygon(g,-30,-110,-25,-147,-8,-112);polygon(g,30,-110,25,-147,8,-112);}
                case "bird" -> {if(detail.equals("owl")){polygon(g,-39,-105,-37,-143,-8,-116);polygon(g,39,-105,37,-143,8,-116);}if(detail.equals("chick")){g.setStroke(new BasicStroke(4));g.drawArc(-6,-145,18,28,30,150);}}
                case "robot" -> {g.setStroke(new BasicStroke(4));g.drawLine(0,-126,0,-150);g.setColor(accent);oval(g,-7,-161,14,14);}
            }
            g.setColor(fur);if(robot)round(g,-46,-126,92,77,20);else oval(g,-47,-125,94,78);
        }
        if(detail.equals("fox")){g.setColor(secondary);polygon(g,-44,-88,-7,-62,-27,-50);polygon(g,44,-88,7,-62,27,-50);}
        if(detail.equals("panda")||detail.equals("mask")){g.setColor(fur.darker().darker());if(detail.equals("mask"))round(g,-40,-105,80,30,18);else{oval(g,-35,-106,28,35);oval(g,7,-106,28,35);}}
        if(detail.equals("owl")||detail.equals("penguin")){g.setColor(secondary);oval(g,-39,-113,42,57);oval(g,-3,-113,42,57);}
        if(detail.equals("gills")){g.setColor(PINK);g.setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));for(int side:new int[]{-1,1})for(int i=0;i<3;i++)g.drawLine(side*42,-97+i*10,side*65,-117+i*22);}
        if(detail.equals("koala")){g.setColor(INK);oval(g,-10,-96,20,28);}
        patterns(g,pet.pattern,secondary);
        double eyeY=ghost?-72:slime?-42:-93;
        if(shape.equals("frog"))eyeY=-116;
        boolean closed=f.hovered()||a==PetBehavior.Action.BLINK||a==PetBehavior.Action.GROOM||a==PetBehavior.Action.YAWN||(pet.animated&&Math.cos(t*.51)>.998);
        g.setStroke(new BasicStroke(2.7f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        Color eyes=PetCatalog.color(pet.eyeColor);if(eyes.equals(fur))eyes=INK;
        for(int side:new int[]{-1,1}){
            double ex=side*19+(a==PetBehavior.Action.WATCH?Math.sin(t*.5)*4:0);
            g.setColor(eyes);
            if(closed)g.draw(new Arc2D.Double(ex-6,eyeY-3,12,8,0,180,Arc2D.OPEN));
            else if(pet.eyes.equals("Звёздные"))star(g,ex,eyeY,7,3);
            else {double radius=pet.eyes.equals("Большие")?10:6;oval(g,ex-radius/2,eyeY-5,radius,pet.eyes.equals("Сонные")?5:11);g.setColor(new Color(255,255,255,190));oval(g,ex-1,eyeY-4,2.5,2.5);}
        }
        g.setColor(PINK);oval(g,-4,eyeY+13,8,5);
        g.setColor(INK);
        if(bird){g.setColor(new Color(225,161,82));polygon(g,-8,eyeY+13,8,eyeY+13,0,eyeY+23);}
        else if(a==PetBehavior.Action.YAWN){oval(g,-6,eyeY+23,12,15);}
        else {g.draw(new Arc2D.Double(-8,eyeY+13,8,12,180,160,Arc2D.OPEN));g.draw(new Arc2D.Double(0,eyeY+13,8,12,200,160,Arc2D.OPEN));}
        if(detail.equals("teeth")){g.setColor(Color.WHITE);round(g,-5,eyeY+28,10,10,2);}
        if(detail.equals("whiskers")||detail.equals("mouse")){g.setColor(INK);for(int side:new int[]{-1,1}){g.drawLine(side*27,(int)eyeY+14,side*47,(int)eyeY+10);g.drawLine(side*27,(int)eyeY+21,side*47,(int)eyeY+24);}}
        if(detail.equals("cheeks")||detail.equals("shine")){g.setColor(new Color(255,207,210,150));oval(g,-36,eyeY+10,18,11);oval(g,18,eyeY+10,18,11);}
        if(a==PetBehavior.Action.GROOM||a==PetBehavior.Action.WAVE||a==PetBehavior.Action.SNIFF){g.setColor(fur.brighter());double lift=a==PetBehavior.Action.WAVE?Math.sin(t*3)*10:Math.sin(t*2)*5;oval(g,19,-92+lift,18,35);}
        if(robot){g.setColor(accent);round(g,-14,-48,28,17,5);g.setColor(secondary);g.drawLine(-8,-40,8,-40);}
        wardrobe(g,pet,accent,eyeY);
    }
    private static void patterns(Graphics2D g,String pattern,Color c){
        g.setColor(c);g.setStroke(new BasicStroke(3));
        switch(pattern){
            case "Пятна" -> {oval(g,-29,-39,12,16);oval(g,15,-29,10,12);oval(g,23,-108,13,10);}
            case "Полоски" -> {for(int i=0;i<3;i++){g.drawLine(-24+i*22,-116,-18+i*18,-105);g.drawLine(-31,-42+i*10,-18,-39+i*10);}}
            case "Веснушки" -> {for(int side:new int[]{-1,1})for(int i=0;i<3;i++)oval(g,side*(23+i*4)-2,-82+i%2*5,3,3);}
            case "Звёздочки" -> {star(g,-25,-32,7,3);star(g,28,-109,6,2.5);}
            case "Носочки" -> {round(g,-35,-8,29,13,8);round(g,7,-8,29,13,8);}
        }
    }
    private static void back(Graphics2D g,String back,Color c,double t){
        g.setColor(c.darker());
        switch(back){
            case "Плащ героя","Плащ мага" -> {Path2D p=new Path2D.Double();p.moveTo(-26,-53);p.curveTo(-80,-30,-48+Math.sin(t)*8,10,56,5);p.lineTo(28,-50);p.closePath();g.fill(p);if(back.equals("Плащ мага")){g.setColor(c.brighter());star(g,-41,-19,8,3);}}
            case "Рюкзак" -> {round(g,22,-58,39,55,14);g.setColor(c.brighter());round(g,36,-40,18,20,5);}
            case "Крылья феи","Крылья ангела" -> {g.setColor(back.equals("Крылья ангела")?new Color(236,237,246):new Color(c.getRed(),c.getGreen(),c.getBlue(),170));for(int s:new int[]{-1,1}){g.rotate(s*.3,0,-50);oval(g,s<0?-83:27,-98,56,62);g.rotate(-s*.3,0,-50);}}
            case "Джетпак" -> {g.setColor(new Color(117,131,155));round(g,21,-61,39,57,10);g.setColor(new Color(246,190,96));polygon(g,33,-6,53,-6,43+Math.sin(t)*4,20);}
        }
    }
    private static void wardrobe(Graphics2D g,PetModel p,Color c,double eyeY){
        g.setColor(c);
        switch(p.neck){
            case "Шарф" -> {round(g,-29,-51,58,10,7);round(g,18,-46,11,33,5);}
            case "Бант","Бабочка" -> {polygon(g,-18,-50,0,-42,-18,-32);polygon(g,18,-50,0,-42,18,-32);oval(g,-4,-45,8,8);}
            case "Медальон" -> {g.setStroke(new BasicStroke(3));g.drawArc(-26,-54,52,29,180,180);g.setColor(new Color(235,195,100));oval(g,-7,-35,14,14);}
            case "Галстук" -> {polygon(g,-6,-49,6,-49,9,-20,0,-12,-9,-20);}
            case "Бусы" -> {for(int i=0;i<7;i++)oval(g,-25+i*7,-42+Math.sin(i*Math.PI/6)*10,7,7);}
        }
        g.setColor(c);g.setStroke(new BasicStroke(3));
        switch(p.face){
            case "Круглые очки" -> {g.draw(new Ellipse2D.Double(-34,eyeY-11,29,24));g.draw(new Ellipse2D.Double(5,eyeY-11,29,24));g.drawLine(-5,(int)eyeY,5,(int)eyeY);}
            case "Солнечные очки" -> {g.setColor(INK);round(g,-35,eyeY-10,30,21,6);round(g,5,eyeY-10,30,21,6);g.drawLine(-5,(int)eyeY,5,(int)eyeY);}
            case "Монокль" -> {g.draw(new Ellipse2D.Double(5,eyeY-11,29,25));g.drawLine(31,(int)eyeY+9,35,(int)eyeY+40);}
            case "Маска героя" -> {g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),160));round(g,-38,eyeY-13,76,25,10);}
            case "Визор" -> {g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),140));round(g,-39,eyeY-14,78,29,11);g.setColor(new Color(255,255,255,150));g.drawLine(-24,(int)eyeY-7,15,(int)eyeY-7);}
        }
        g.setColor(c);
        switch(p.head){
            case "Шапка" -> {g.fill(new Arc2D.Double(-36,-157,72,61,0,180,Arc2D.CHORD));round(g,-38,-134,76,11,7);oval(g,-8,-169,16,16);}
            case "Берет" -> {oval(g,-40,-146,80,25);round(g,-4,-153,7,13,4);}
            case "Корона" -> {polygon(g,-34,-129,-39,-155,-14,-144,0,-168,14,-144,39,-155,34,-129);}
            case "Колпак мага" -> {polygon(g,-34,-131,-8,-194,27,-131);oval(g,-45,-139,90,14);g.setColor(c.brighter());star(g,-8,-156,8,3);}
            case "Цилиндр" -> {round(g,-27,-176,54,48,5);oval(g,-43,-138,86,14);g.setColor(c.brighter());round(g,-27,-144,54,8,3);}
            case "Цветок" -> {g.setColor(PINK);for(int i=0;i<5;i++){double a=i*Math.PI*2/5;oval(g,28+Math.cos(a)*11-8,-132+Math.sin(a)*11-8,16,16);}g.setColor(new Color(240,209,106));oval(g,22,-138,12,12);}
            case "Наушники" -> {g.setStroke(new BasicStroke(6));g.draw(new Arc2D.Double(-51,-144,102,84,0,180,Arc2D.OPEN));round(g,-55,-116,16,35,8);round(g,39,-116,16,35,8);}
            case "Нимб" -> {g.setColor(new Color(237,206,112));g.setStroke(new BasicStroke(4));g.draw(new Ellipse2D.Double(-30,-171,60,13));}
            case "Рога" -> {polygon(g,-32,-125,-45,-165,-20,-140);polygon(g,32,-125,45,-165,20,-140);}
        }
    }
    private static void props(Graphics2D g,PetModel pet,PetBehavior.Frame f,Color c){
        double t=f.phase(),p=f.progress();PetBehavior.Action a=f.action();
        g.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        switch(a){
            case EMERGE_BOX,HIDE_BOX -> {g.setColor(new Color(187,147,107));round(g,-55,-40,110,46,5);g.setColor(new Color(221,182,139));polygon(g,-55,-40,-75,-60,-12,-57,0,-40);polygon(g,55,-40,75,-60,12,-57,0,-40);g.setColor(new Color(165,124,89));g.drawLine(0,-39,0,5);}
            case PLAY_BALL -> {double x=53+Math.sin(t)*19;g.setColor(c);oval(g,x,-24-Math.abs(Math.sin(t))*12,25,25);g.setColor(c.brighter());g.drawArc((int)x,-24-(int)(Math.abs(Math.sin(t))*12),25,25,50,170);}
            case READ -> {g.setColor(c);polygon(g,-40,-43,-1,-36,-1,-5,-40,-13);polygon(g,40,-43,1,-36,1,-5,40,-13);g.setColor(new Color(242,237,218));g.drawLine(-30,-30,-10,-25);g.drawLine(10,-25,30,-30);}
            case TYPE -> {g.setColor(new Color(86,99,126));round(g,-40,-42,80,40,5);g.setColor(c);oval(g,-5,-28,10,10);g.setColor(new Color(166,177,196));round(g,-46,-5,92,9,3);}
            case EAT -> {g.setColor(new Color(235,184,114));for(int i=0;i<4;i++)oval(g,-22+i*13,-20-Math.sin(t+i)*3,10,9);g.setColor(c);g.fill(new Arc2D.Double(-43,-26,86,43,180,180,Arc2D.CHORD));g.setColor(c.brighter());round(g,-46,-24,92,8,5);}
            case DRINK -> {g.setColor(c);round(g,24,-38,27,33,7);g.setStroke(new BasicStroke(4));g.draw(new Arc2D.Double(43,-34,20,22,-90,180,Arc2D.OPEN));g.setColor(new Color(150,160,170,120));g.draw(new CubicCurve2D.Double(33,-46,23,-55,45,-60,34,-70));}
            case HEART -> {g.setColor(PINK);double y=-150-p*35;heart(g,0,y,15+Math.sin(t)*3);}
            case STAR_GAZE,WATCH,CHASE_SPARK -> {g.setColor(new Color(233,203,115));double x=Math.sin(t*.5)*65,y=-147+Math.cos(t*.6)*15;star(g,x,y,9,4);if(a==PetBehavior.Action.STAR_GAZE){star(g,-45,-173,5,2);star(g,49,-157,6,3);}}
            case SNIFF -> {g.setColor(new Color(119,168,132));g.drawLine(46,-4,46,-56);g.setColor(PINK);for(int i=0;i<5;i++)oval(g,40+Math.cos(i*1.26)*9,-62+Math.sin(i*1.26)*9,12,12);g.setColor(c);oval(g,42,-60,8,8);}
            case DROP_IN -> {g.setColor(c);g.fill(new Arc2D.Double(-75,-224,150,84,0,180,Arc2D.CHORD));g.setStroke(new BasicStroke(3));g.drawLine(0,-184,0,-121);g.setColor(c.brighter());g.drawLine(0,-222,0,-184);}
            case VANISH,TELEPORT,APPEAR_PORTAL -> {g.setColor(c);for(int i=0;i<6;i++){double angle=i*Math.PI/3+t;star(g,Math.cos(angle)*(35+p*35),-65+Math.sin(angle)*(45+p*25),4+2*Math.sin(t+i),2);}}
            case FLOAT -> {g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),65));g.draw(new Ellipse2D.Double(-61,-159,122,161));}
            case PEEK_EDGE -> {g.setColor(new Color(123,137,159));round(g,-58,-5,116,8,4);}
            default -> {}
        }
    }
    private static void bubble(Graphics2D original,int width,int height,String speech,double scale){
        Graphics2D g=(Graphics2D)original.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));
            String text=speech;
            while(text.length()>1&&g.getFontMetrics().stringWidth(text)>width-30)text=text.substring(0,text.length()-2)+"…";
            int tw=g.getFontMetrics().stringWidth(text),bw=Math.min(width-8,tw+20),x=(width-bw)/2;
            int y=Math.max(4,height-24-(int)(240*scale)-29);
            g.setColor(new Color(246,244,252,242));round(g,x,y,bw,25,12);g.setColor(INK);
            Shape old=g.getClip();g.clipRect(x+7,y,bw-14,25);g.drawString(text,x+10,y+17);g.setClip(old);
        }finally{g.dispose();}
    }
    private static void oval(Graphics2D g,double x,double y,double w,double h){g.fill(new Ellipse2D.Double(x,y,w,h));}
    private static void round(Graphics2D g,double x,double y,double w,double h,double radius){g.fill(new RoundRectangle2D.Double(x,y,w,h,radius,radius));}
    private static void polygon(Graphics2D g,double... pts){Path2D p=new Path2D.Double();p.moveTo(pts[0],pts[1]);for(int i=2;i<pts.length;i+=2)p.lineTo(pts[i],pts[i+1]);p.closePath();g.fill(p);}
    private static void star(Graphics2D g,double x,double y,double r,double inner){Path2D p=new Path2D.Double();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5,v=(i%2==0?r:inner);if(i==0)p.moveTo(x+Math.cos(a)*v,y+Math.sin(a)*v);else p.lineTo(x+Math.cos(a)*v,y+Math.sin(a)*v);}p.closePath();g.fill(p);}
    private static void heart(Graphics2D g,double x,double y,double r){Path2D p=new Path2D.Double();p.moveTo(x,y+r);p.curveTo(x-r*2,y-r*.2,x-r,y-r*1.7,x,y-r*.5);p.curveTo(x+r,y-r*1.7,x+r*2,y-r*.2,x,y+r);g.fill(p);}
}
