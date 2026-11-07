package dev.codepet;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.function.*;

public final class PetToolWindow implements ToolWindowFactory,DumbAware {
    @Override public void createToolWindowContent(Project project,ToolWindow window){
        PetPanel panel=new PetPanel(PetState::collection,()->window.hide(null));
        Content content=ContentFactory.getInstance().createContent(panel,"",false);content.setDisposer(panel);window.getContentManager().addContent(content);
    }
    static final class PetPanel extends JPanel implements Disposable {
        private final Supplier<PetCollection> collection;
        private final ArrayList<Runnable> bindings=new ArrayList<>();
        private final JLabel species=PetUi.label("● АКТИВНЫЙ",12,PetUi.MUTED),counter=PetUi.label("1 / 1",11,PetUi.MUTED),level=PetUi.label("УРОВЕНЬ 1 · 0 XP",13,PetUi.ACCENT),activity=PetUi.label("Исследует свой мир",12,PetUi.MUTED),stats=PetUi.label("Статистика",12,PetUi.MUTED);
        private final JTextField name=new JTextField();
        private final JProgressBar xp=new JProgressBar();
        private final PetCanvas canvas;
        private final Timer timer;
        private final JPanel creation=PetUi.card();
        private String displayedId="";
        private int configuration;
        private boolean syncing,disposed;
        PetPanel(Supplier<PetCollection> collection,Runnable hide){
            super(new BorderLayout());this.collection=collection;setBackground(PetUi.BG);
            canvas=new PetCanvas(this::model);
            JPanel body=PetUi.vertical();body.setBackground(PetUi.BG);body.setOpaque(true);body.setBorder(new EmptyBorder(18,18,18,18));
            JPanel header=new JPanel(new BorderLayout());header.setOpaque(false);
            JLabel brand=PetUi.label("CODE PET",22,PetUi.TEXT);brand.setFont(brand.getFont().deriveFont(Font.BOLD));header.add(brand);
            header.add(PetUi.label("● Автосохранение",10,new Color(133,202,172)),BorderLayout.EAST);body.add(header);
            body.add(Box.createVerticalStrut(5));body.add(PetUi.label("Маленький мир рядом с твоим кодом",12,PetUi.MUTED));body.add(Box.createVerticalStrut(18));
            JPanel hero=PetUi.card();
            JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);top.add(species);top.add(counter,BorderLayout.EAST);hero.add(top,BorderLayout.NORTH);
            JPanel stage=new JPanel(new BorderLayout(6,0));stage.setOpaque(false);
            JButton previous=PetUi.button("‹",false),next=PetUi.button("›",false);
            previous.setToolTipText("Предыдущий питомец");next.setToolTipText("Следующий питомец");previous.getAccessibleContext().setAccessibleName("Предыдущий питомец");next.getAccessibleContext().setAccessibleName("Следующий питомец");
            previous.addActionListener(e->{data().select(-1);refresh();});next.addActionListener(e->{data().select(1);refresh();});
            stage.add(navigation(previous),BorderLayout.WEST);stage.add(canvas);stage.add(navigation(next),BorderLayout.EAST);hero.add(stage);
            JPanel summary=PetUi.vertical();
            name.setOpaque(false);name.setForeground(PetUi.TEXT);name.setCaretColor(PetUi.ACCENT);name.setHorizontalAlignment(JTextField.CENTER);name.setFont(new Font(Font.SANS_SERIF,Font.BOLD,24));name.setBorder(new EmptyBorder(5,0,5,0));name.setToolTipText("Имя сохраняется во время ввода");
            name.getDocument().addDocumentListener(new DocumentListener(){private void save(){if(!syncing){String text=name.getText().strip();model().name=text.isBlank()?PetCatalog.species(model().type).name():text.substring(0,Math.min(40,text.length()));}}public void insertUpdate(DocumentEvent e){save();}public void removeUpdate(DocumentEvent e){save();}public void changedUpdate(DocumentEvent e){save();}});
            summary.add(name);level.setHorizontalAlignment(SwingConstants.CENTER);summary.add(level);summary.add(Box.createVerticalStrut(10));
            xp.setStringPainted(true);xp.setForeground(PetUi.ACCENT);xp.setBackground(PetUi.LINE);xp.setBorderPainted(false);xp.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));xp.setPreferredSize(new Dimension(200,18));summary.add(xp);summary.add(Box.createVerticalStrut(10));activity.setHorizontalAlignment(SwingConstants.CENTER);summary.add(activity);
            JPanel actions=new JPanel(new FlowLayout(FlowLayout.CENTER,8,6));actions.setOpaque(false);JButton pet=PetUi.button("♡ Погладить",false),play=PetUi.button("Поиграть",false);pet.addActionListener(e->canvas.react("Мне нравится!"));play.addActionListener(e->canvas.play());actions.add(pet);actions.add(play);summary.add(actions);
            hero.add(summary,BorderLayout.SOUTH);body.add(hero);body.add(Box.createVerticalStrut(12));
            JButton add=PetUi.button("＋ Создать питомца",true);add.addActionListener(e->{creation.setVisible(!creation.isVisible());revalidate();});body.add(add);body.add(Box.createVerticalStrut(12));
            buildCreation();body.add(creation);creation.setVisible(false);
            JTabbedPane tabs=new JTabbedPane();tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI(){
                @Override protected void installDefaults(){super.installDefaults();tabInsets=new Insets(8,10,8,10);tabAreaInsets=new Insets(0,0,7,0);contentBorderInsets=new Insets(0,0,0,0);selectedTabPadInsets=new Insets(0,0,0,0);}
                @Override protected void paintTabBackground(Graphics g,int placement,int index,int x,int y,int w,int h,boolean selected){g.setColor(selected?PetUi.LINE:PetUi.BG);g.fillRoundRect(x,y,w,h,10,10);}
                @Override protected void paintTabBorder(Graphics g,int placement,int index,int x,int y,int w,int h,boolean selected){}
                @Override protected void paintContentBorder(Graphics g,int placement,int selected){}
                @Override protected void paintFocusIndicator(Graphics g,int placement,Rectangle[] rects,int index,Rectangle icon,Rectangle text,boolean selected){}
                @Override protected void paintText(Graphics g,int placement,Font font,FontMetrics metrics,int index,String title,Rectangle rect,boolean selected){g.setFont(font);g.setColor(selected?PetUi.ACCENT:PetUi.MUTED);g.drawString(title,rect.x,rect.y+metrics.getAscent());}
            });tabs.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);tabs.setOpaque(false);tabs.setBackground(PetUi.CARD);tabs.setForeground(PetUi.TEXT);tabs.setBorder(new EmptyBorder(5,0,0,0));
            tabs.addTab("Внешность",appearance());tabs.addTab("Гардероб",wardrobe());tabs.addTab("Прогресс",progress());tabs.addTab("Поведение",settings());body.add(tabs);
            body.add(Box.createVerticalStrut(12));JButton hideButton=PetUi.button("Скрыть панель",false);hideButton.addActionListener(e->hide.run());body.add(hideButton);
            // Creation changes its height when opened; leave it unconstrained within the scroll view.
            for(Component c:body.getComponents())if(c instanceof JComponent j){j.setAlignmentX(Component.LEFT_ALIGNMENT);if(c!=creation)j.setMaximumSize(new Dimension(Integer.MAX_VALUE,j.getPreferredSize().height));}
            JScrollPane scroll=new JScrollPane(body);scroll.setBorder(BorderFactory.createEmptyBorder());scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);scroll.getVerticalScrollBar().setUnitIncrement(18);scroll.getViewport().setBackground(PetUi.BG);add(scroll);
            timer=new Timer(40,e->pulse());
            addHierarchyListener(e->{if((e.getChangeFlags()&HierarchyEvent.SHOWING_CHANGED)!=0){if(isShowing()&&!disposed)timer.start();else timer.stop();}});
            refresh();
        }
        private JPanel navigation(JButton button){JPanel p=new JPanel(new GridBagLayout());p.setOpaque(false);p.add(button);return p;}
        private PetCollection data(){return collection.get();}private PetModel model(){return data().active();}
        private void buildCreation(){
            JPanel fields=PetUi.vertical();fields.add(PetUi.label("НОВЫЙ СПУТНИК",12,PetUi.ACCENT));fields.add(Box.createVerticalStrut(10));
            JComboBox<PetCatalog.Species> type=new JComboBox<>(PetCatalog.SPECIES);styleCombo(type);fields.add(PetUi.row(PetUi.label("Вид",12,PetUi.MUTED),type));
            JTextField newName=new JTextField("Новый друг");newName.setBackground(PetUi.BG);newName.setForeground(PetUi.TEXT);newName.setCaretColor(PetUi.ACCENT);fields.add(PetUi.row(PetUi.label("Имя",12,PetUi.MUTED),newName));
            JButton create=PetUi.button("Добавить в коллекцию",true);create.addActionListener(e->{PetCatalog.Species selected=(PetCatalog.Species)type.getSelectedItem();if(selected!=null){data().create(selected.id(),newName.getText());creation.setVisible(false);refresh();revalidate();}});fields.add(create);creation.add(fields);
        }
        private JPanel page(){JPanel p=PetUi.vertical();p.setBackground(PetUi.CARD);p.setOpaque(true);p.setBorder(new EmptyBorder(16,14,16,14));return p;}
        private JPanel appearance(){JPanel p=page();p.add(PetUi.label("ПАЛИТРА · 16 ОТТЕНКОВ",11,PetUi.ACCENT));
            p.add(palette("Основной цвет",m->m.color,(m,v)->m.color=v));p.add(palette("Живот, уши и узор",m->m.secondaryColor,(m,v)->m.secondaryColor=v));
            p.add(combo("Узор",PetCatalog.PATTERNS,m->m.pattern,(m,v)->m.pattern=v));p.add(combo("Глаза",PetCatalog.EYES,m->m.eyes,(m,v)->m.eyes=v));
            p.add(combo("Цвет глаз",PetCatalog.COLORS,m->m.eyeColor,(m,v)->m.eyeColor=v));p.add(combo("Хвост",PetCatalog.TAILS,m->m.tail,(m,v)->m.tail=v));
            p.add(slider("Размер в панели",56,112,m->m.size,(m,v)->m.size=v));PetUi.fit(p);return p;}
        private JPanel wardrobe(){JPanel p=page();p.add(PetUi.label("СОБЕРИ СВОЙ ОБРАЗ",11,PetUi.ACCENT));
            p.add(combo("Голова",PetCatalog.HEADS,m->m.head,(m,v)->m.head=v));p.add(combo("Очки и маски",PetCatalog.FACES,m->m.face,(m,v)->m.face=v));
            p.add(combo("Украшение",PetCatalog.NECKS,m->m.neck,(m,v)->m.neck=v));p.add(combo("Плащ и спина",PetCatalog.BACKS,m->m.back,(m,v)->m.back=v));
            p.add(palette("Цвет аксессуаров",m->m.accessoryColor,(m,v)->m.accessoryColor=v));
            p.add(PetUi.label("Косметика доступна сразу. Выбирай свободно.",11,PetUi.MUTED));PetUi.fit(p);return p;}
        private JPanel progress(){JPanel p=page();p.add(PetUi.label("РАСТЁТ ВМЕСТЕ С КОДОМ",11,PetUi.ACCENT));p.add(Box.createVerticalStrut(12));p.add(stats);p.add(Box.createVerticalStrut(16));
            for(String s:new String[]{"+2 XP за новую строку длиннее 10 символов","+5 XP за каждые 100 новых символов","Пробелы и отдельные комментарии не считаются","Повторный ввод удалённого объёма без XP","Новый файл — автоматический перекус, без XP","Игры и поглаживания не дают XP","Следующий уровень стоит 100 × текущий уровень"}){p.add(PetUi.label(s,11,PetUi.MUTED));p.add(Box.createVerticalStrut(8));}
            PetUi.fit(p);return p;}
        private JPanel settings(){JPanel p=page();p.add(PetUi.label("ТИХОЕ ПРИСУТСТВИЕ",11,PetUi.ACCENT));p.add(Box.createVerticalStrut(8));
            p.add(check("Питомец в свободной области редактора",m->m.roaming,(m,v)->m.roaming=v));p.add(check("Анимации · 36 самостоятельных сцен",m->m.animated,(m,v)->m.animated=v));
            p.add(check("Редкие реплики",m->m.speech,(m,v)->m.speech=v));p.add(check("Приостановить начисление XP",m->m.paused,(m,v)->m.paused=v));
            p.add(combo("Характер",PetCatalog.TEMPERAMENTS,m->m.temperament,(m,v)->m.temperament=v));p.add(slider("Размер в редакторе",22,40,m->m.roamingSize,(m,v)->m.roamingSize=v));
            p.add(PetUi.label("Если безопасного места нет, питомец скрывается.",11,PetUi.MUTED));p.add(Box.createVerticalStrut(5));p.add(PetUi.label("Наведение — жмурится. Клики проходят к коду.",11,PetUi.MUTED));PetUi.fit(p);return p;}
        private JPanel combo(String title,String[] values,Function<PetModel,String> get,BiConsumer<PetModel,String> set){
            JComboBox<String> box=new JComboBox<>(values);styleCombo(box);box.addActionListener(e->{if(!syncing)set.accept(model(),(String)box.getSelectedItem());});bindings.add(()->box.setSelectedItem(get.apply(model())));return PetUi.row(PetUi.label(title,12,PetUi.MUTED),box);}
        private void styleCombo(JComboBox<?> box){box.setBackground(PetUi.BG);box.setForeground(PetUi.TEXT);box.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));box.setMaximumRowCount(12);box.setFocusable(true);}
        private JCheckBox check(String title,Predicate<PetModel> get,BiConsumer<PetModel,Boolean> set){
            JCheckBox box=new JCheckBox(title);box.setOpaque(false);box.setForeground(PetUi.TEXT);box.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));box.setBorder(new EmptyBorder(7,0,7,0));box.addActionListener(e->{if(!syncing)set.accept(model(),box.isSelected());});bindings.add(()->box.setSelected(get.test(model())));return box;}
        private JPanel slider(String title,int min,int max,ToIntFunction<PetModel> get,ObjIntConsumer<PetModel> set){
            JSlider slider=new JSlider(min,max);slider.setOpaque(false);slider.setForeground(PetUi.ACCENT);slider.addChangeListener(e->{if(!syncing)set.accept(model(),slider.getValue());});bindings.add(()->slider.setValue(get.applyAsInt(model())));return PetUi.row(PetUi.label(title,12,PetUi.MUTED),slider);}
        private JPanel palette(String title,Function<PetModel,String> get,BiConsumer<PetModel,String> set){
            JPanel outer=PetUi.vertical();outer.setBorder(new EmptyBorder(10,0,7,0));outer.add(PetUi.label(title,12,PetUi.MUTED));outer.add(Box.createVerticalStrut(6));JPanel grid=new JPanel(new GridLayout(2,8,5,5));grid.setOpaque(false);
            for(String color:PetCatalog.COLORS){JButton swatch=new JButton(){@Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();try{g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(PetCatalog.color(color));g.fillRoundRect(3,3,getWidth()-6,getHeight()-6,9,9);if(color.equals(get.apply(model()))){g.setColor(PetUi.TEXT);g.setStroke(new BasicStroke(2));g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,11,11);g.setColor(PetUi.BG);g.drawLine(getWidth()/2-4,getHeight()/2,getWidth()/2-1,getHeight()/2+3);g.drawLine(getWidth()/2-1,getHeight()/2+3,getWidth()/2+5,getHeight()/2-4);}}finally{g.dispose();}}};
                swatch.setPreferredSize(new Dimension(26,27));swatch.setOpaque(false);swatch.setContentAreaFilled(false);swatch.setBorderPainted(false);swatch.setToolTipText(color);swatch.getAccessibleContext().setAccessibleName(title+": "+color);swatch.addActionListener(e->{set.accept(model(),color);grid.repaint();canvas.repaint();});grid.add(swatch);}
            bindings.add(grid::repaint);outer.add(grid);return outer;
        }
        void pulse(){refresh();canvas.tick();}
        private void refresh(){
            PetModel pet=model();boolean changed=!displayedId.equals(pet.id);
            int signature=java.util.Objects.hash(pet.id,pet.name,pet.color,pet.secondaryColor,pet.eyeColor,pet.accessoryColor,pet.pattern,pet.head,pet.face,pet.neck,pet.back,pet.eyes,pet.tail,pet.temperament,pet.size,pet.roamingSize,pet.animated,pet.roaming,pet.paused,pet.speech);
            if(changed||configuration!=signature){displayedId=pet.id;configuration=signature;syncing=true;if(changed||!name.isFocusOwner())name.setText(pet.name);bindings.forEach(Runnable::run);syncing=false;}
            species.setText("● АКТИВНЫЙ · "+PetCatalog.species(pet.type).name());counter.setText((data().pets.indexOf(pet)+1)+" / "+data().pets.size());
            level.setText("УРОВЕНЬ "+pet.level()+"  ·  "+pet.xp+" XP");xp.setMaximum(pet.nextLevelCost());xp.setValue(pet.levelProgress());xp.setString(pet.levelProgress()+" / "+pet.nextLevelCost()+" XP до нового уровня");
            activity.setText(pet.paused?"Начисление XP на паузе":canvas.actionLabel());stats.setText("<html>Символов: "+pet.charactersWritten+"<br>Строк: "+pet.linesWritten+"<br>Создано файлов: "+pet.filesCreated+"</html>");
        }
        @Override public void dispose(){disposed=true;timer.stop();}
    }
}
