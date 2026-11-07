package dev.codepet;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class PetChecks {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        Path output=Path.of(args[0]);Files.createDirectories(output);
        model();collection();metrics();behavior();stories(output);exploration();safety();persistence(output);
        SwingUtilities.invokeAndWait(()->{try{render(output);ui(output);}catch(Exception e){throw new RuntimeException(e);}});
        check(new PetStartup() instanceof com.intellij.openapi.startup.ProjectActivity,"Startup matches SDK extension interface");
        check(new PetFileListener() instanceof com.intellij.openapi.vfs.newvfs.BulkFileListener,"VFS listener matches SDK interface");
        System.out.println("PASS: "+checks+" checks; model, collection, XP, migration, persistence, behavior, placement, hover, renderer and UI");
    }
    static void model(){
        PetModel p=new PetModel();check(p.level()==1,"Initial level");check(p.earn(99,0)==0,"Character threshold");check(p.earn(1,0)==5,"Character remainder carries");
        check(p.earn(250,3)==16,"Proportional combined reward");check(p.characterRemainder==50&&p.linesWritten==3&&p.charactersWritten==350,"Statistics");
        p.paused=true;int old=p.xp;check(p.earn(100,1)==0&&p.xp==old,"Pause blocks rewards");p.paused=false;
        check(p.earn(-10,-20)==0,"Invalid deltas don't subtract progress");
        for(int level=1;level<=200;level++){p.xp=(int)(50L*level*(level-1));check(p.level()==level&&p.levelProgress()==0,"Level boundary "+level);p.xp+=p.nextLevelCost()-1;check(p.level()==level&&p.levelProgress()==p.nextLevelCost()-1,"Before next level "+level);}
        p.xp=Integer.MAX_VALUE-1;check(p.earn(Integer.MAX_VALUE,Integer.MAX_VALUE)==1&&p.xp==Integer.MAX_VALUE,"XP overflow clamps");
        p.charactersWritten=Long.MAX_VALUE-1;p.earn(10,0);check(p.charactersWritten==Long.MAX_VALUE,"Statistics overflow clamps");
        p.name=null;p.type="bad";p.color=null;p.head=null;p.roamingSize=1000;p.size=-1;p.xp=-100;p.normalize();
        check(p.name.equals("Байт")&&p.type.equals("cat")&&p.roamingSize==40&&p.size==56&&p.xp==0,"Corrupt character normalizes");
    }
    static void collection(){
        PetCollection c=new PetCollection();PetModel first=c.active();first.xp=111;
        PetModel second=c.create("fox","Лис");check(c.pets.size()==2&&c.active()==second&&second.xp==0,"Creation activates one new profile");
        c.select(1);check(c.active()==first,"Carousel wraps right");c.select(-1);check(c.active()==second,"Carousel wraps left");check(first.xp==111,"Switch preserves individual XP");
        c.file("test",0,0);check(c.record("test",100,1,first.id)==7&&first.xp==118&&second.xp==0,"Reward credited to originating profile");
        check(c.record("test",0,0,second.id)==0&&c.record("test",100,1,second.id)==0,"Delete/retype and switching cannot replay reward");
        check(c.record("test",200,2,second.id)==7,"Growth above previous high-water earns XP");
        second.paused=true;c.record("test",300,3,second.id);second.paused=false;
        check(c.record("test",300,3,second.id)==0,"Pause creates no reward backlog");
        second.id=first.id;c.activeId="missing";c.normalize();check(!c.pets.get(0).id.equals(c.pets.get(1).id)&&c.active()!=null,"Duplicate/missing IDs repaired");
        check(PetCatalog.SPECIES.length>=15&&PetCatalog.SPECIES.length<=30,"Species count contract");check(PetCatalog.COLORS.length>=10&&PetCatalog.COLORS.length<=20,"Palette count contract");
        Set<String> ids=new HashSet<>();for(PetCatalog.Species s:PetCatalog.SPECIES)check(ids.add(s.id()),"Unique species ID");
    }
    static void metrics(){
        check(!PetEligibility.ignored("/app/build/sandbox/project/example.py","/app/build/sandbox/project"),"SDK sandbox project is not mistaken for build output");
        check(PetEligibility.ignored("/app/project/build/output.py","/app/project"),"Generated files inside project excluded");
        check(PetEligibility.ignored("/app/project/.idea/settings.xml","/app/project"),"IDE metadata excluded");
        check(PetMetrics.count("").characters()==0,"Empty metrics");check(PetMetrics.count("1234567890\n12345678901").lines()==1,"Strict >10 threshold");
        check(PetMetrics.count(" a b c \t\n\r\n").characters()==3,"Whitespace ignored");
        check(PetMetrics.count("# comment with enough chars\n// comment\n/* block start\n* block line\n-- comment\nvalue = 1234567890").lines()==1,"Standalone comment prefixes ignored");
        check(PetMetrics.count("привет_мир = 123\n").lines()==1,"Unicode source accepted");
        check(PetCodingTracker.key("file:///a.py").equals(PetCodingTracker.key("file:///a.py"))&&PetCodingTracker.key("file:///a.py").length()==64,"Stable nonplaintext file identifier");
    }
    static void behavior(){
        PetBehavior b=new PetBehavior(new Random(42));check(PetBehavior.Action.values().length>=50,"Scenario count contract");
        EnumSet<PetBehavior.Action> seen=EnumSet.noneOf(PetBehavior.Action.class);boolean bounded=true;
        for(int i=0;i<100_000;i++){b.update(.1,i%2==0?1:500,true);seen.add(b.action());bounded&=b.position()>=0&&b.position()<=1&&b.frame().progress()>=0&&b.frame().progress()<=1;}
        b.eat();seen.add(b.action());check(bounded&&seen.size()>=50,"Every scenario reachable and stays bounded");
        b.force(PetBehavior.Action.FALL_HOLE);for(int i=0;i<26;i++)b.update(.1,300,true);check(b.action()==PetBehavior.Action.HIDDEN,"Fall transitions offstage");
        for(int i=0;i<41;i++)b.update(.1,300,true);check(b.action().ordinal()<4,"Hidden transitions to entrance");
        double phase=b.phase(),position=b.position();b.update(.1,300,false);check(b.phase()==phase&&b.position()==position,"Animation disable freezes");
        b.hover(true);b.update(.1,300,true);check(b.frame().hovered()&&b.phase()==phase,"Hover squints and pauses movement");
        b.hover(false);b.play();check(b.action()==PetBehavior.Action.PLAY_BALL,"Play is cosmetic scene");
        PetDialogue d=new PetDialogue();PetModel p=new PetModel();d.say("Привет");check(d.text(p).equals("Привет"),"Speech visible");p.speech=false;check(d.text(p).isEmpty(),"Speech can be disabled");
    }
    static void stories(Path output)throws Exception{
        StringBuilder catalog=new StringBuilder("# Каталог историй 0.6\n\nУ каждого вида 12 цепочек: шесть общих историй с собственной анатомией и шесть видовых с особыми предметами и эффектами. Действия переиспользуются между историями; это не 288 независимо нарисованных мультфильмов. Все сцены косметические, без изменения исходного текста и без XP.\n\n");
        for(PetCatalog.Species species:PetCatalog.SPECIES){
            PetModel pet=new PetModel();pet.type=species.id();var recipes=PetStories.forType(pet.type);check(recipes.size()>=10&&recipes.size()<=20,"Story count "+pet.type);
            catalog.append("## ").append(species.name()).append("\n\n");
            Set<String> names=new HashSet<>();
            for(PetStories.Story story:recipes){
                catalog.append("- **").append(story.name()).append("**: ").append(String.join(" → ",Arrays.stream(story.beats()).map(action->action.label).toList())).append(".\n");
                check(names.add(story.name())&&story.beats().length>=4,"Distinct multi-step story "+pet.type+"/"+story.name());
                PetBehavior actor=new PetBehavior(new Random(42));actor.autonomy(pet,true);actor.startStory(story);boolean completed=false;
                for(int tick=0;tick<(story.duration()+10)*10;tick++){actor.update(.1,300,true);if(actor.consumeCompleted()==1){completed=true;break;}}
                check(completed&&pet.xp==0,"Story completes without XP "+story.name());
            }
        }
        Files.writeString(output.resolve("story-catalog.md"),catalog.toString());
        check(PetMotion.jumpHeight(0)==0&&PetMotion.jumpHeight(Math.PI)>20&&PetMotion.jumpSquash(.09*Math.PI*2)>0,"Jump has anticipation, flight and landing");
        PetMotion motion=new PetMotion();motion.reset(0,0);int previous=-1;
        for(int i=0;i<100;i++){Point at=motion.approach(200,80,.04);check(at.x>=previous&&at.x<=200&&at.y<=80,"Motion approaches without overshoot or reversal");previous=at.x;}
        PetModel p=new PetModel();p.xp=100;check(p.availableFinds()==1&&p.receiveGift()&&!p.receiveGift()&&p.discoveries==1,"Level find delivered once without auto-equipping");p.xp=2100;check(p.availableFinds()==6,"All furnishings obtainable through code levels");
        check(PetCatalog.unlocked("Шарф путешественника",2)&&!PetCatalog.unlocked("Шарф путешественника",1)&&PetCatalog.unlocked("Звёздный плащ",4),"Found wardrobe pieces follow level thresholds");
        PetReactions.signal(p,"tests-passed",1_000_000_000L);long reaction=p.ideSignal;PetReactions.signal(p,"tests-failed",2_000_000_000L);check(p.ideSignal==reaction,"Test and paste reactions are rate limited");PetReactions.signal(p,"large-paste",17_000_000_000L);check(PetReactions.action(p.ideEvent)==PetBehavior.Action.WATCH,"Large paste reaction is cosmetic");
        check(PetExecutionListener.isTest("PythonTestsConfiguration","PythonTests")&&!PetExecutionListener.isTest("PythonRunConfiguration","Python"),"Test reaction filters generic runs");
    }
    static void exploration(){
        PetExploration world=new PetExploration(0,new Random(7));
        world.update(9_999_999_999L,true,true,true);check(world.mode()==PetExploration.Mode.CLASSIC,"Before ten seconds stays classic");
        world.update(10_000_000_000L,true,true,true);check(world.mode()==PetExploration.Mode.EXPLORE,"Ten seconds mouse idle allows exploration");
        world.extend(60);check(world.visitingAt(80_000_000_000L),"Long story has no wall-clock cutoff");
        check(world.typing(12_000_000_000L)&&world.mode()==PetExploration.Mode.CLASSIC,"Typing returns exploration to safe classic mode");
        for(long t=13_000_000_000L;t<90_000_000_000L;t+=1_000_000_000L){world.typing(t);world.update(t,true,true,true);check(world.mode()!=PetExploration.Mode.EXPLORE,"Stationary mouse does not cause code visits during continuous typing");}
        world=new PetExploration(0,new Random(11));world.update(46_000_000_000L,true,true,true);check(world.away(),"Away requires inactivity of keyboard and mouse");
        check(world.mouseActivity(47_000_000_000L)&&world.mode()==PetExploration.Mode.CLASSIC,"Mouse immediately cancels long story");
        world=new PetExploration(0);world.update(60_000_000_000L,true,false,false);check(world.mode()==PetExploration.Mode.CLASSIC,"Optional modes can be disabled");
        world=new PetExploration(0,new Random(42));EnumSet<PetExploration.Place> places=EnumSet.noneOf(PetExploration.Place.class);boolean grew=false;
        for(int i=0;i<200;i++){world.update(50_000_000_000L+i*100_000_000_000L,true,true,true);places.add(world.place());grew|=world.enlarged();world.completeStory(50_000_000_000L+i*100_000_000_000L);check(world.alpha()>=0&&world.alpha()<=1,"Visit alpha bounded");}
        check(places.size()==4&&grew,"Exploration varies locations and occasionally grows");
        world.pause(0);world.update(46_000_000_000L,false,true,true);check(!world.visiting(),"Animation disabled suppresses long stories");
        Rectangle area=new Rectangle(10,20,900,600),avoid=new Rectangle(350,180,220,110);Dimension sprite=new Dimension(156,120);
        check(PetScenePlacement.fit(area,sprite,new Point(360,190),avoid)==null,"Active typing area remains protected");
        Point from=new Point(10,20),to=new Point(810,520);check(PetScenePlacement.interpolate(from,to,.5).equals(new Point(410,270)),"Trajectory remains smooth");
    }
    static void safety(){
        Rectangle view=new Rectangle(100,200,900,600);Dimension sprite=new Dimension(156,120);
        Rectangle lane=PetSafeArea.lane(view,sprite,java.util.List.of(new Rectangle(100,200,520,600)));
        check(lane!=null&&lane.x>=620&&view.contains(lane),"Finds margin away from code");
        check(PetSafeArea.lane(view,sprite,java.util.List.of(view))==null,"Full code viewport hides pet");
        check(PetSafeArea.lane(new Rectangle(0,0,40,40),sprite,java.util.List.of())==null,"Tiny viewport hides pet");
        Random random=new Random(11);
        for(int run=0;run<300;run++){
            ArrayList<Rectangle> obstacles=new ArrayList<>();for(int j=0;j<15;j++)obstacles.add(new Rectangle(random.nextInt(1100),random.nextInt(900),30+random.nextInt(200),30+random.nextInt(180)));
            Rectangle safe=PetSafeArea.lane(view,sprite,obstacles);
            if(safe!=null){check(view.contains(safe)&&safe.width>=sprite.width&&safe.height==sprite.height,"Safe lane fits viewport");for(Rectangle r:obstacles)check(!safe.intersects(r),"Entire motion lane avoids every obstacle");}
        }
    }
    static void persistence(Path out)throws Exception{
        PetCollection c=new PetCollection();c.active().name="Байт";c.active().xp=230;c.active().head="Корона";c.active().face="Круглые очки";c.active().pattern="Полоски";
        PetModel fox=c.create("fox","Лиса");fox.back="Плащ мага";fox.color="Индиго";fox.characterRemainder=89;fox.filesCreated=7;fox.idleExploration=false;fox.cameos=false;fox.growWhenIdle=false;fox.activityLevel="Спокойный";c.quietUntilMillis=123456789;fox.storiesCompleted=12;fox.discoveries=1;
        c.file("abc",200,3);c.record("abc",300,4,fox.id);
        Element xml=XmlSerializer.serialize(c);String saved=new org.jdom.output.XMLOutputter().outputString(xml);Files.writeString(out.resolve("test-state.xml"),saved);
        Element fromDisk=new org.jdom.input.SAXBuilder().build(out.resolve("test-state.xml").toFile()).getRootElement();
        PetCollection restored=XmlSerializer.deserialize(fromDisk,PetCollection.class);restored.normalize();
        check(restored.quietUntilMillis==123456789&&restored.pets.size()==2&&restored.activeId.equals(fox.id)&&restored.active().back.equals("Плащ мага")&&restored.active().characterRemainder==89&&restored.active().filesCreated==7&&!restored.active().idleExploration&&!restored.active().cameos&&!restored.active().growWhenIdle&&restored.active().activityLevel.equals("Спокойный")&&restored.active().storiesCompleted==12,"Full collection disk roundtrip");
        check(restored.pets.get(0).xp==230&&restored.pets.get(0).head.equals("Корона")&&restored.pets.get(0).face.equals("Круглые очки"),"Inactive character wardrobe and XP preserved");
        check(restored.record("abc",300,4,fox.id)==0,"File high-water survives restart");
        Element old=new Element("PetModel");for(String[] v:new String[][]{{"xp","230"},{"name","Старый Байт"},{"color","Лавандовый"},{"roaming","false"},{"sleeping","true"},{"food","4"}})old.addContent(new Element("option").setAttribute("name",v[0]).setAttribute("value",v[1]));
        PetCollection migrated=XmlSerializer.deserialize(old,PetCollection.class);migrated.normalize();check(migrated.pets.size()==1&&migrated.active().xp==230&&migrated.active().name.equals("Старый Байт")&&!migrated.active().roaming,"0.1/0.2 save migrates");
        check(migrated.active().idleExploration&&migrated.active().cameos,"Old saves receive enabled exploration defaults");
        PetState service=new PetState();service.loadState(restored);check(service.getState().activeId.equals(fox.id),"Persistent service accepts restored collection");
        PetCollection snapshot=service.getState();restored.active().name="Changed after save";restored.create("owl","New");
        check(snapshot.pets.size()==2&&!snapshot.active().name.equals("Changed after save"),"Save snapshot independent of live UI edits");
    }
    static BufferedImage petImage(PetModel pet,PetBehavior.Frame frame){BufferedImage image=new BufferedImage(240,280,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();PetPainter.paint(g,240,280,pet,frame,"",.9);g.dispose();return image;}
    static void render(Path out)throws Exception{
        BufferedImage gallery=new BufferedImage(1200,1120,BufferedImage.TYPE_INT_RGB);Graphics2D g=gallery.createGraphics();g.setColor(PetUi.BG);g.fillRect(0,0,1200,1120);
        for(int i=0;i<PetCatalog.SPECIES.length;i++){
            PetModel p=new PetModel();p.type=PetCatalog.SPECIES[i].id();p.color=PetCatalog.COLORS[i%16];p.secondaryColor="Снежный";
            PetBehavior.Frame f=new PetBehavior.Frame(PetBehavior.Action.SIT,2.5,.5,1,false);
            BufferedImage im=petImage(p,f);g.drawImage(im,i%6*200,i/6*280,200,260,null);g.setColor(PetUi.TEXT);g.drawString(PetCatalog.SPECIES[i].name(),i%6*200+20,i/6*280+270);
            for(PetBehavior.Action a:PetBehavior.Action.values())for(double progress:new double[]{0,.5,1}){
                BufferedImage frame=petImage(p,new PetBehavior.Frame(a,2.5,progress,-1,false));int visible=0;for(int y=0;y<frame.getHeight();y++)for(int x=0;x<frame.getWidth();x++)if((frame.getRGB(x,y)>>>24)>0)visible++;
                check(a==PetBehavior.Action.HIDDEN?visible==0:visible>20,"Rendered pixels "+p.type+"/"+a+"/"+progress);
            }
        }
        g.dispose();ImageIO.write(gallery,"png",out.resolve("pets-gallery.png").toFile());
        BufferedImage scenes=new BufferedImage(960,(PetBehavior.Action.values().length-36)*210,BufferedImage.TYPE_INT_RGB);Graphics2D sceneGraphics=scenes.createGraphics();
        sceneGraphics.setColor(PetUi.BG);sceneGraphics.fillRect(0,0,scenes.getWidth(),scenes.getHeight());
        for(int index=36;index<PetBehavior.Action.values().length;index++){
            PetBehavior.Action action=PetBehavior.Action.values()[index];PetModel actor=new PetModel();actor.type=PetCatalog.SPECIES[(index*7)%24].id();actor.color=PetCatalog.COLORS[index%16];actor.back="Плащ героя";actor.face="Круглые очки";
            for(int moment=0;moment<3;moment++){Graphics2D cell=(Graphics2D)sceneGraphics.create(moment*320,(index-36)*210,320,210);
                PetPainter.paint(cell,320,200,actor,new PetBehavior.Frame(action,1+moment*2.7,.15+moment*.33,1,false,'λ'),"",.65);
                cell.setColor(PetUi.TEXT);cell.drawString(action.label+" · "+(moment+1),12,17);cell.dispose();}
        }
        sceneGraphics.dispose();ImageIO.write(scenes,"png",out.resolve("new-scenes.png").toFile());
        BufferedImage homes=new BufferedImage(1020,220,BufferedImage.TYPE_INT_RGB);Graphics2D rooms=homes.createGraphics();for(int i=0;i<3;i++){PetModel resident=new PetModel();resident.xp=new int[]{0,300,2100}[i];Graphics2D room=(Graphics2D)rooms.create(i*340,0,340,220);PetHome.paint(room,340,220,resident);room.dispose();}rooms.dispose();ImageIO.write(homes,"png",out.resolve("homes.png").toFile());
        PetModel p=new PetModel();BufferedImage open=petImage(p,new PetBehavior.Frame(PetBehavior.Action.SIT,2.5,.5,1,false)),closed=petImage(p,new PetBehavior.Frame(PetBehavior.Action.SIT,2.5,.5,1,true));boolean changed=false;
        for(int y=100;y<220;y++)for(int x=60;x<180;x++)changed|=open.getRGB(x,y)!=closed.getRGB(x,y);check(changed,"Hover changes actual eye pixels");
        BufferedImage variants=new BufferedImage(1200,1120,BufferedImage.TYPE_INT_RGB);Graphics2D speciesGraphics=variants.createGraphics();speciesGraphics.setColor(PetUi.BG);speciesGraphics.fillRect(0,0,1200,1120);
        for(int index=0;index<24;index++){PetModel member=new PetModel();member.type=PetCatalog.SPECIES[index].id();member.color=PetCatalog.COLORS[index%16];
            for(int variant=0;variant<6;variant++){BufferedImage specimen=petImage(member,new PetBehavior.Frame(PetBehavior.Action.SPECIES_TRICK,2.5,.6,1,false,'λ',"species-"+variant));check(specimen!=null,"Species trick renders "+member.type+"/"+variant);if(variant==0)speciesGraphics.drawImage(specimen,index%6*200,index/6*280,200,260,null);}
            speciesGraphics.setColor(PetUi.TEXT);speciesGraphics.drawString(PetCatalog.SPECIES[index].name(),index%6*200+15,index/6*280+270);
        }
        speciesGraphics.dispose();ImageIO.write(variants,"png",out.resolve("species-stories.png").toFile());
        PetModel ghost=new PetModel();ghost.type="ghost";BufferedImage eyes=petImage(ghost,new PetBehavior.Frame(PetBehavior.Action.SPECIES_TRICK,2.5,.7,1,false,';',"species-0"));BufferedImage body=petImage(ghost,new PetBehavior.Frame(PetBehavior.Action.SIT,2.5,.7,1,false));int eyePixels=0,bodyPixels=0;
        for(int y=0;y<eyes.getHeight();y++)for(int x=0;x<eyes.getWidth();x++){if(eyes.getRGB(x,y)>>>24>0)eyePixels++;if(body.getRGB(x,y)>>>24>0)bodyPixels++;}
        check(eyePixels>20&&eyePixels<bodyPixels/3,"Ghost loses its body while eyes remain visible");
        PetPlayScene.Surface stable=new PetPlayScene.Surface();stable.setBounds(0,0,800,600);stable.setVisible(true);Rectangle layerBounds=stable.getBounds();
        for(int i=0;i<100;i++){stable.present(new Rectangle(i*3,100,156,140));check(stable.getBounds().equals(layerBounds)&&stable.isVisible(),"Actor movement never moves or toggles its Swing layer");}
        for(String color:PetCatalog.COLORS){p.color=color;petImage(p,new PetBehavior.Frame(PetBehavior.Action.SIT,2.5,.5,1,false));}
        for(String[] options:new String[][]{PetCatalog.HEADS,PetCatalog.FACES,PetCatalog.NECKS,PetCatalog.BACKS,PetCatalog.PATTERNS,PetCatalog.EYES,PetCatalog.TAILS})for(String option:options){
            if(Arrays.asList(PetCatalog.HEADS).contains(option))p.head=option;else if(Arrays.asList(PetCatalog.FACES).contains(option))p.face=option;else if(Arrays.asList(PetCatalog.NECKS).contains(option))p.neck=option;else if(Arrays.asList(PetCatalog.BACKS).contains(option))p.back=option;else if(Arrays.asList(PetCatalog.PATTERNS).contains(option))p.pattern=option;else if(Arrays.asList(PetCatalog.EYES).contains(option))p.eyes=option;else p.tail=option;
            petImage(p,new PetBehavior.Frame(PetBehavior.Action.WAVE,2.5,.5,1,true));check(true,"Accessory renders "+option);
        }
        JLayeredPane pane=new JLayeredPane();pane.setSize(500,300);JPanel editor=new JPanel();editor.setBounds(0,0,500,300);pane.add(editor,JLayeredPane.DEFAULT_LAYER);PetPlayScene.Surface overlay=new PetPlayScene.Surface();overlay.setBounds(50,50,150,150);pane.add(overlay,JLayeredPane.PALETTE_LAYER);
        check(!overlay.isFocusable()&&pane.findComponentAt(100,100)==editor,"Overlay passes actual Swing hit test to editor");
    }
    static java.util.List<Component> all(Container root){ArrayList<Component> result=new ArrayList<>();for(Component c:root.getComponents()){result.add(c);if(c instanceof Container container)result.addAll(all(container));}return result;}
    static JButton button(Container root,String text){return (JButton)all(root).stream().filter(c->c instanceof JButton b&&b.getText().equals(text)).findFirst().orElseThrow();}
    static void layout(Container root){root.invalidate();root.doLayout();for(Component c:root.getComponents())if(c instanceof Container container)layout(container);}
    static void ui(Path out)throws Exception{
        PetCollection collection=new PetCollection();collection.active().xp=150;PetModel second=collection.create("fox","Люми");second.head="Берет";second.neck="Шарф";second.color="Лавандовый";
        PetToolWindow.PetPanel panel=new PetToolWindow.PetPanel(()->collection,()->{});panel.setSize(480,1000);layout(panel);for(int i=0;i<80;i++)panel.pulse();
        check(all(panel).stream().noneMatch(c->c instanceof JTabbedPane),"Main panel contains no settings tabs");
        int before=second.xp;button(panel,"♡ Погладить").doClick();check(second.xp==before,"Interaction does not award XP");
        button(panel,"Убрать с экрана").doClick();check(!collection.enabled(second.id)&&collection.enabled(collection.pets.get(0).id),"Explicit action puts away only the chosen pet");
        button(panel,"Выпустить в редактор").doClick();check(collection.activePets().size()==2,"Release button retains other active pets");
        button(panel,"‹").doClick();check(collection.active().type.equals("cat")&&collection.activePets().size()==2,"Arrows only choose the card");button(panel,"›").doClick();
        PetPreferences preferences=panel.preferencesContent();preferences.setSize(480,700);layout(preferences);
        JCheckBox behind=(JCheckBox)all(preferences).stream().filter(c->c instanceof JCheckBox b&&b.getText().equals("Питомцы позади текста кода")).findFirst().orElseThrow();behind.doClick();check(!collection.behindText,"Popup settings change persisted global preferences");behind.doClick();
        collection.select(-1);panel.pulse();String firstName=collection.active().name;
        JTextField field=(JTextField)all(preferences).stream().filter(c->c instanceof JTextField f&&f.getToolTipText()!=null&&f.getToolTipText().contains("Имя сохраняется")).findFirst().orElseThrow();field.setText("Новое имя");
        check(second.name.equals("Новое имя")&&collection.active().name.equals(firstName),"Popup remains pinned to its pet when the carousel changes");collection.select(1);panel.pulse();
        button(panel,"＋ Добавить питомца").doClick();button(panel,"Создать и выпустить").doClick();check(collection.pets.size()==3&&collection.active().xp==0&&collection.enabled(collection.activeId),"Creation clearly releases the new pet");
        int earned=collection.active().xp;button(panel,"Скрыть всех на 30 мин").doClick();check(collection.quietUntilMillis>System.currentTimeMillis()&&collection.active().xp==earned,"Quiet mode hides everyone without XP changes");button(panel,"Вернуть на экран").doClick();check(collection.quietUntilMillis==0,"Release action also resumes an enabled pet from quiet mode");
        BufferedImage image=new BufferedImage(480,1000,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();layout(panel);panel.printAll(g);g.dispose();ImageIO.write(image,"png",out.resolve("ui-preview.png").toFile());
        preferences.refresh();layout(preferences);BufferedImage popup=new BufferedImage(480,700,BufferedImage.TYPE_INT_RGB);Graphics2D preview=popup.createGraphics();preferences.printAll(preview);preview.dispose();ImageIO.write(popup,"png",out.resolve("settings-preview.png").toFile());
        for(Component c:all(preferences))if(c instanceof JTabbedPane tabs)for(int index=0;index<tabs.getTabCount();index++){tabs.setSelectedIndex(index);layout(preferences);Graphics2D page=popup.createGraphics();preferences.printAll(page);page.dispose();}
        preferences.dispose();panel.dispose();check(true,"Main panel and popup pages render and dispose");
    }

}
