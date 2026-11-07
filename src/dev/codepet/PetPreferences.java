package dev.codepet;

import com.intellij.openapi.Disposable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.*;

/** Popup-only settings, pinned to a profile ID even when the main carousel changes. */
final class PetPreferences extends JPanel implements Disposable {
    private final Supplier<PetCollection> collection;private final String petId;private final Runnable onChanged;
    private final ArrayList<Runnable> bindings=new ArrayList<>();private boolean syncing,disposed;private int configuration;
    private final JTextField name=new JTextField();private final Timer timer;
    private final JLabel stats=PetUi.label("",12,PetUi.TEXT),finds=PetUi.label("",12,PetUi.MUTED);private PetHome home;
    PetPreferences(Supplier<PetCollection> collection,String petId,Runnable onChanged){
        super(new BorderLayout(0,10));this.collection=collection;this.petId=petId;this.onChanged=onChanged;
        setBackground(PetUi.BG);setBorder(new EmptyBorder(14,14,14,14));
        name.setBackground(PetUi.CARD);name.setForeground(PetUi.TEXT);name.setCaretColor(PetUi.ACCENT);name.setToolTipText("Имя сохраняется во время ввода");
        name.getDocument().addDocumentListener(new DocumentListener(){private void save(){if(!syncing){String value=name.getText().strip();model().name=value.isEmpty()?PetCatalog.species(model().type).name():value.substring(0,Math.min(40,value.length()));onChanged.run();}}public void insertUpdate(DocumentEvent e){save();}public void removeUpdate(DocumentEvent e){save();}public void changedUpdate(DocumentEvent e){save();}});
        add(PetUi.row(PetUi.label("Имя питомца",12,PetUi.TEXT),name),BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
        JPanel look=PetUi.vertical();look.add(appearance());look.add(wardrobe());tabs.addTab("Внешность",scroll(look));tabs.addTab("Поведение",scroll(settings()));tabs.addTab("Прогресс",scroll(progress()));add(tabs);
        add(PetUi.label("Изменения сохраняются автоматически",11,PetUi.MUTED),BorderLayout.SOUTH);
        timer=new Timer(250,e->refresh());addHierarchyListener(e->{if((e.getChangeFlags()&java.awt.event.HierarchyEvent.SHOWING_CHANGED)!=0){if(isShowing()&&!disposed)timer.start();else timer.stop();}});refresh();
    }
    private PetCollection data(){return collection.get();}private PetModel model(){return data().find(petId);}
    private JScrollPane scroll(JPanel content){JScrollPane scroll=new JScrollPane(content);scroll.setBorder(BorderFactory.createEmptyBorder());scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);scroll.getViewport().setBackground(PetUi.CARD);scroll.getVerticalScrollBar().setUnitIncrement(20);return scroll;}
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
            p.add(PetUi.label("Базовые вещи сразу; находки — за уровни.",11,PetUi.MUTED));PetUi.fit(p);return p;}
        private JPanel progress(){JPanel p=page();p.add(PetUi.label("РАСТЁТ ВМЕСТЕ С КОДОМ",11,PetUi.ACCENT));p.add(Box.createVerticalStrut(12));p.add(stats);p.add(Box.createVerticalStrut(16));
            for(String s:new String[]{"+2 XP за новую строку длиннее 10 символов","+5 XP за каждые 100 новых символов","Пробелы и отдельные комментарии не считаются","Повторный ввод удалённого объёма без XP","Новый файл — автоматический перекус, без XP","Игры и поглаживания не дают XP","Следующий уровень стоит 100 × текущий уровень"}){p.add(PetUi.label(s,11,PetUi.MUTED));p.add(Box.createVerticalStrut(8));}
            home=new PetHome(this::model);p.add(Box.createVerticalStrut(12));p.add(home);p.add(finds);PetUi.fit(p);return p;}
        private JPanel settings(){JPanel p=page();p.add(PetUi.label("ПОВЕДЕНИЕ ЭТОГО ПИТОМЦА",11,PetUi.ACCENT));
            p.add(PetUi.label("Для всех питомцев",11,PetUi.ACCENT));
            p.add(collectionCheck("Питомцы позади текста кода",c->c.behindText,(c,v)->c.behindText=v));
            p.add(PetUi.label("<html>Выключено: самостоятельные прогулки только в Project.<br>Игры по клавишам всё равно появляются под текстом.</html>",10,PetUi.MUTED));
            p.add(collectionCheck("Игры от Enter, Tab и удержания Delete",c->c.keyboardGames,(c,v)->c.keyboardGames=v));
            p.add(Box.createVerticalStrut(12));p.add(PetUi.label("Только этот питомец",11,PetUi.ACCENT));
            p.add(check("Анимации этого питомца",m->m.animated,(m,v)->m.animated=v));p.add(check("Реплики этого питомца",m->m.speech,(m,v)->m.speech=v));
            p.add(combo("Характер",PetCatalog.TEMPERAMENTS,m->m.temperament,(m,v)->m.temperament=v));p.add(slider("Размер",22,40,m->m.roamingSize,(m,v)->m.roamingSize=v));
            p.add(check("Пауза XP этого питомца",m->m.paused,(m,v)->m.paused=v));
            p.add(PetUi.label("<html>Enter: мяч с отскоками и прыжками.<br>Tab: шарик и порывы ветра.<br>Удерживай Delete/Backspace: кресло с попкорном.<br><br>Клавиши продолжают редактировать код.<br>Игры можно отключить здесь.</html>",11,PetUi.MUTED));PetUi.fit(p);return p;}
        private JCheckBox collectionCheck(String title,Predicate<PetCollection> get,BiConsumer<PetCollection,Boolean> set){JCheckBox box=new JCheckBox(title);box.setOpaque(false);box.setForeground(PetUi.TEXT);box.setBorder(new EmptyBorder(8,0,8,0));box.addActionListener(e->{if(!syncing){set.accept(data(),box.isSelected());onChanged.run();}});bindings.add(()->box.setSelected(get.test(data())));return box;}
        private JPanel combo(String title,String[] values,Function<PetModel,String> get,BiConsumer<PetModel,String> set){
            JComboBox<String> box=new JComboBox<>(values);styleCombo(box);box.addActionListener(e->{if(!syncing){set.accept(model(),(String)box.getSelectedItem());onChanged.run();}});bindings.add(()->{box.removeAllItems();for(String option:values)if(PetCatalog.unlocked(option,model().level()))box.addItem(option);box.setSelectedItem(get.apply(model()));});return PetUi.row(PetUi.label(title,12,PetUi.MUTED),box);}
        private void styleCombo(JComboBox<?> box){box.setBackground(PetUi.BG);box.setForeground(PetUi.TEXT);box.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));box.setMaximumRowCount(12);box.setFocusable(true);}
        private JCheckBox check(String title,Predicate<PetModel> get,BiConsumer<PetModel,Boolean> set){
            JCheckBox box=new JCheckBox(title);box.setOpaque(false);box.setForeground(PetUi.TEXT);box.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));box.setBorder(new EmptyBorder(7,0,7,0));box.addActionListener(e->{if(!syncing){set.accept(model(),box.isSelected());onChanged.run();}});bindings.add(()->box.setSelected(get.test(model())));return box;}
        private JPanel slider(String title,int min,int max,ToIntFunction<PetModel> get,ObjIntConsumer<PetModel> set){
            JSlider slider=new JSlider(min,max);slider.setOpaque(false);slider.setForeground(PetUi.ACCENT);slider.addChangeListener(e->{if(!syncing){set.accept(model(),slider.getValue());onChanged.run();}});bindings.add(()->slider.setValue(get.applyAsInt(model())));return PetUi.row(PetUi.label(title,12,PetUi.MUTED),slider);}
        private JPanel palette(String title,Function<PetModel,String> get,BiConsumer<PetModel,String> set){
            JPanel outer=PetUi.vertical();outer.setBorder(new EmptyBorder(10,0,7,0));outer.add(PetUi.label(title,12,PetUi.MUTED));outer.add(Box.createVerticalStrut(6));JPanel grid=new JPanel(new GridLayout(2,8,5,5));grid.setOpaque(false);
            for(String color:PetCatalog.COLORS){JButton swatch=new JButton(){@Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();try{g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(PetCatalog.color(color));g.fillRoundRect(3,3,getWidth()-6,getHeight()-6,9,9);if(color.equals(get.apply(model()))){g.setColor(PetUi.TEXT);g.setStroke(new BasicStroke(2));g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,11,11);g.setColor(PetUi.BG);g.drawLine(getWidth()/2-4,getHeight()/2,getWidth()/2-1,getHeight()/2+3);g.drawLine(getWidth()/2-1,getHeight()/2+3,getWidth()/2+5,getHeight()/2-4);}}finally{g.dispose();}}};
                swatch.setPreferredSize(new Dimension(26,27));swatch.setOpaque(false);swatch.setContentAreaFilled(false);swatch.setBorderPainted(false);swatch.setToolTipText(color);swatch.getAccessibleContext().setAccessibleName(title+": "+color);swatch.addActionListener(e->{set.accept(model(),color);grid.repaint();onChanged.run();});grid.add(swatch);}
            bindings.add(grid::repaint);outer.add(grid);return outer;
        }

    void refresh(){
        PetModel p=model();int signature=java.util.Objects.hash(p.id,p.name,p.color,p.secondaryColor,p.eyeColor,p.accessoryColor,p.pattern,p.head,p.face,p.neck,p.back,p.eyes,p.tail,p.temperament,p.size,p.roamingSize,p.animated,p.paused,p.speech,p.level(),data().behindText,data().keyboardGames);
        if(configuration!=signature){configuration=signature;syncing=true;try{if(!name.isFocusOwner()&&!name.getText().equals(p.name))name.setText(p.name);bindings.forEach(Runnable::run);}finally{syncing=false;}}
        stats.setText("<html>Уровень: "+p.level()+" · "+p.xp+" XP<br>Символов: "+p.charactersWritten+"<br>Строк: "+p.linesWritten+"<br>Создано файлов: "+p.filesCreated+"<br>Завершено историй: "+p.storiesCompleted+"</html>");
        finds.setText("Находки: "+p.availableFinds()+" / 6 · "+p.latestFind());if(home!=null)home.repaint();
    }
    @Override public void dispose(){disposed=true;timer.stop();}
}
