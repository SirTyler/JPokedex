package me.sirtyler.pokedex.tools;

import me.sirtyler.pokedex.JPokedex;
import me.sirtyler.pokedex.JPokemon;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBuilder {
    private static final int end = 807;
    private static ExecutorService serviceA = Executors.newSingleThreadExecutor();
    private static ExecutorService serviceB = Executors.newSingleThreadExecutor();
    private static ExecutorService serviceC = Executors.newSingleThreadExecutor();
    private static ExecutorService serviceD = Executors.newSingleThreadExecutor();
    private static DBConnector db;
    private static HashMap<Integer, ArrayList<JPokemon>> map = new HashMap<>();
    private static ArrayList<JPokemon> list = new ArrayList<>();
    private JProgressBar pkBar;
    private JButton pkDownload;
    private JButton pkFill;
    private JButton pkUpdate;
    private JPanel contentPane;
    private JProgressBar pkTotal;
    private JButton pkLoad;
    private JList pkList;
    private DefaultListModel<JPokemon> listModel;

    public static void main(String[] args) {
        db = new DBConnector();
        db.open();
        db.load(map, list, null);

        DBuilder dBuilder = new DBuilder();
        JFrame frame = new JFrame("Database Builder");
        frame.setContentPane(dBuilder.contentPane);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 400, 400);
        frame.setVisible(true);
        SwingUtilities.invokeLater(dBuilder::setUp);
    }

    private void setUp() {
        listModel = new DefaultListModel<>();
        pkList.setModel(listModel);
        pkBar.setMaximum(end);
        pkTotal.setMaximum(end);

        //----Full Re-Download/Download---//
        pkDownload.addActionListener(evt -> {
                    pkDownload.setEnabled(false);

                    serviceA.submit(() -> {
                        System.out.println("Downloading all Pokemon");

                        for (int i = 0; i <= end; i++) {
                            List<JPokemon> pList = JPokemon.build(JPokedex.pokeApi.getPokemon(i));

                            for (JPokemon pk : pList) {
                                addData(pk);
                                saveData(pk);

                                final int n = i;
                                final JPokemon p = pk;
                                EventQueue.invokeLater(() -> {
                                    listModel.addElement(p);
                                    pkBar.setValue((n));
                                });
                            }

                            if (i % 10 == 0) db.forceWrite();
                            if (i % 300 == 0) {
                                try {
                                    System.out.println("Going idle for a few seconds.");
                                    Thread.sleep(60000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        db.forceWrite();
                        System.out.println("Database Complete.");

                        EventQueue.invokeLater(() -> {
                            pkDownload.setEnabled(true);
                            pkBar.setValue(0);
                        });
                    });
                }
        );

        //----Fill Missing Pokemon in Database---//
        pkFill.addActionListener(evt -> {
                    pkFill.setEnabled(false);

                    serviceB.submit(() -> {
                        ArrayList<Integer> list = new ArrayList<>();
                        Map<Integer, ArrayList<JPokemon>> m = (Map<Integer, ArrayList<JPokemon>>) map.clone();
                        for (int i = 0; i <= end; i++) {
                            if (!m.containsKey(i)) list.add(i);
                        }

                        System.out.println(list.size() + " EMPTY ENTRIES");
                        for (Integer i : list) {
                            List<JPokemon> pList = JPokemon.build(JPokedex.pokeApi.getPokemon(i));

                            for (JPokemon pk : pList) {
                                addData(pk);
                                saveData(pk);

                                final int n = i;
                                final JPokemon p = pk;
                                EventQueue.invokeLater(() -> {
                                    listModel.addElement(p);
                                    pkBar.setValue((n));
                                });
                            }
                        }

                        db.forceWrite();
                        System.out.println("Database Fixed.");

                        EventQueue.invokeLater(() -> {
                            pkFill.setEnabled(true);
                            pkBar.setValue(0);
                        });
                    });
                }
        );

        //----Update Phonetics----//
        pkUpdate.addActionListener(evt -> {
                    pkUpdate.setEnabled(false);

                    serviceC.submit(() -> {
                        InputStream resource = JPokedex.class.getResourceAsStream("/phon.txt");
                        int i = 0;
                        String str;
                        try {
                            BufferedReader br = new BufferedReader(new InputStreamReader(resource));
                            if (resource != null) {
                                while ((str = br.readLine()) != null) {
                                    String[] data = str.trim().split(";");
                                    JPokemon pk = getData(data[0].trim());
                                    if (pk != null) {
                                        pk.phon = data[1].trim();
                                        addPhonetic(pk);

                                        final int n = i++;
                                        final JPokemon p = pk;
                                        EventQueue.invokeLater(() -> {
                                            listModel.addElement(p);
                                            pkBar.setValue((n));
                                        });
                                    } else
                                        System.out.println("Error on " + str);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                resource.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        db.forceWrite();
                        System.out.println("Database Fixed.");

                        EventQueue.invokeLater(() -> {
                            pkUpdate.setEnabled(true);
                            pkBar.setValue(0);
                        });
                    });
                }
        );

        //----Load Database---//
        pkLoad.addActionListener(evt -> {
                    pkLoad.setEnabled(false);

                    serviceD.submit(() -> {
                        ResultSet rs;

                        try {
                            Statement stmt = db.c.createStatement();
                            rs = stmt.executeQuery("SELECT * FROM pokemon ORDER BY ID_DEX");

                            int i = 0;
                            while (rs.next()) {
                                JPokemon p = new JPokemon();
                                p.load(rs);

                                ArrayList<JPokemon> l = new ArrayList<>();
                                if (map.containsKey(p.id_dex)) l = map.get(p.id_dex);
                                else list.add(p);

                                l.add(p);
                                map.put(p.id_dex, l);

                                final int n = i++;
                                final JPokemon pk = p;
                                EventQueue.invokeLater(() -> {
                                    listModel.addElement(pk);
                                    pkBar.setValue((n));
                                });
                            }
                            rs.close();
                            stmt.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        EventQueue.invokeLater(() -> {
                            pkTotal.setMaximum(end);
                            pkTotal.setValue(pkBar.getValue());
                            pkTotal.setString(pkBar.getValue() + "/" + end);

                            pkUpdate.setEnabled(true);
                            pkDownload.setEnabled(true);
                            pkFill.setEnabled(true);
                            pkBar.setValue(0);
                        });
                    });
                }
        );
    }

    public void addData(JPokemon p) {
        ArrayList<JPokemon> l = new ArrayList<>();
        if (map.containsKey(p.id_dex)) l = map.get(p.id_dex);
        else list.add(p);

        l.add(p);
        map.put(p.id_dex, l);
    }

    public void saveData(JPokemon p) {
        db.insert(p);
    }

    public void updateData(JPokemon p) {
        db.update(p);
    }

    private JPokemon getData(String name) {
        for (Map.Entry<Integer, ArrayList<JPokemon>> entry : map.entrySet()) {
            int key = entry.getKey();
            ArrayList<JPokemon> value = entry.getValue();
            for (JPokemon p : value) {
                if (p.name.equalsIgnoreCase(name)) {
                    return p;
                }
            }
        }
        return null;
    }

    public void addPhonetic(JPokemon p) {
        db.addPhonetic(p);
    }
}
