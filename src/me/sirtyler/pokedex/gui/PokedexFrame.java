package me.sirtyler.pokedex.gui;

import me.sirtyler.pokedex.JPokedex;
import me.sirtyler.pokedex.JPokemon;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

public class PokedexFrame {
    public JPanel contentPane;
    private JList<JPokemon> pkList;
    private JTextField pkSearch;
    private JTabbedPane pkTabs;
    private JButton pkSpeak;
    private JLabel pkType1;
    private JLabel pkType2;
    private JTextPane pkText;
    private JPanel panelLeft;
    private JPanel panelRight;
    private JProgressBar pkHPBar;
    private JProgressBar pkATKBar;
    private JProgressBar pkDEFBar;
    private JProgressBar pkSPATKBar;
    private JLabel pkHP;
    private JLabel pkATK;
    private JLabel pkDEF;
    private JLabel pkSPATK;
    private JProgressBar pkSPDEFBar;
    private JLabel pkSPDEF;
    private JLabel pkSPEED;
    private JProgressBar pkSPEEDBar;
    private JPanel pkDraw;
    private ImageIcon blank;

    public PokedexFrame() {
        //URL url = JPokedex.class.getResource("/res/000.gif");
        URL url = JPokedex.class.getResource("/gifs/substitute.gif");
        blank = new ImageIcon(url);

        pkList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelection(pkList.getSelectedValue());
            }
        });

        pkTabs.addChangeListener(e -> {
            if (pkTabs.getSelectedIndex() >= 0) {
                JPokemon p = JPokedex.instance.getPokemon(pkTabs.getTitleAt(pkTabs.getSelectedIndex()));
                if (p != null) updateSelection2(p);
            }
        });

        pkSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent arg0) {
                try {
                    textChange(arg0);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                try {
                    textChange(arg0);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                if (arg0.getDocument().getLength() <= 0) resetList();
                else try {
                    textChange(arg0);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

            public void textChange(DocumentEvent arg0) throws BadLocationException {
                DefaultListModel<JPokemon> listModel = new DefaultListModel<>();

                for (JPokemon p : JPokedex.instance.getList()) {
                    if (p.name.toUpperCase().contains(arg0.getDocument().getText(0, arg0.getDocument().getLength()).toUpperCase()))
                        listModel.addElement(p);
                }

                pkList.setModel(listModel);
            }
        });
        Dimension d = pkType1.getPreferredSize();
        d.width = 100;
        pkType1.setMinimumSize(d);
        pkType2.setMaximumSize(d);
        pkSearch.setToolTipText("Search");

        d = pkTabs.getPreferredSize();
        pkTabs.setMaximumSize(d);
        pkTabs.setMinimumSize(d);

        pkSpeak.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                JPokemon p = JPokedex.instance.getPokemon(pkTabs.getTitleAt(pkTabs.getSelectedIndex()));
                String trans = JPokedex.instance.translate(p.voice_read);
                JPokedex.instance.speak(trans);
            }
        });
    }

    public void setup() {
        resetList();
        pkList.setSelectedIndex(0);
    }

    public void resetList() {
        ArrayList<JPokemon> pkl = JPokedex.instance.getList();
        JPokemon[] pk = new JPokemon[pkl.size()];
        int i = 0;
        int last_dex = 0;
        for (JPokemon p : pkl) {
            if (last_dex != p.id_dex) pk[i++] = p;
            last_dex = p.id_dex;
        }

        pkList.setListData(pk);
    }

    public void dispose() {
        JPokedex.instance.dispose();
    }

    private void updateSelection(JPokemon pokemon) {
        if (pokemon == null) return;
        pkTabs.removeAll();

        ArrayList<JPokemon> l = JPokedex.instance.getMap().get(pokemon.id);
        for (JPokemon p : l) {
            JLabel pkIcon = new JLabel("", SwingConstants.CENTER);
            try {
                URL url = p.icon;
                ImageIcon img;
                try {
                    img = new ImageIcon(url);
                } catch (Exception e) {
                    p.sprite = null;
                    JPokedex.instance.db.update(p);
                    JPokedex.instance.db.forceWrite();
                    img = blank;
                }
                pkIcon.setIcon(img);
                pkTabs.addTab(p.name.replaceAll("-", " "), pkIcon);
                /*if(!p.name.contains("alola") && p.id_dex < 721) {
                    DrawPanel drawPanel = new DrawPanel(p.icon_ani);
                    pkTabs.addTab(p.name, drawPanel);
                } else {
                    try {
                        BufferedImage img = ImageIO.read(p.icon);
                        ImageIcon i = new ImageIcon(img);
                        pkIcon.setIcon(i);
                    } catch (IOException ex) {
                        URL url = JPokedex.class.getResource("/res/000.gif");
                        ImageIcon i = new ImageIcon(url);
                        pkIcon.setIcon(i);
                        ex.printStackTrace();
                    }
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSelection2(JPokemon pokemon) {
        System.out.println(pokemon);
        String id = String.format("%03d", pokemon.id);

        String type1, type2 = "blank";
        if (pokemon.type.contains("/")) {
            String[] s = pokemon.type.split("/");
            type1 = s[0];
            type2 = s[1];
        } else type1 = pokemon.type;

        ImageIcon t1 = new ImageIcon(JPokedex.class.getResource("/res/" + type1 + ".png"));
        pkType1.setToolTipText(type1);
        pkType1.setIcon(t1);

        ImageIcon t2 = new ImageIcon(JPokedex.class.getResource("/res/" + type2 + ".png"));
        pkType2.setToolTipText(type2);
        pkType2.setIcon(t2);

        pkText.setText(String.format("%s\n%s Type Pokemon\n\n%s", pokemon.name, pokemon.cat, pokemon.desc));
        pkHPBar.setValue(pokemon.b_HP);
        pkHPBar.setString("" + pokemon.b_HP);
        pkATKBar.setValue(pokemon.b_ATK);
        pkATKBar.setString("" + pokemon.b_ATK);
        pkDEFBar.setValue(pokemon.b_DEF);
        pkDEFBar.setString("" + pokemon.b_DEF);
        pkSPATKBar.setValue(pokemon.b_SPATK);
        pkSPATKBar.setString("" + pokemon.b_SPATK);
        pkSPDEFBar.setValue(pokemon.b_SPDEF);
        pkSPDEFBar.setString("" + pokemon.b_SPDEF);
        pkSPEEDBar.setValue(pokemon.b_SPEED);
        pkSPEEDBar.setString("" + pokemon.b_SPEED);
    }
}
