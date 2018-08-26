package me.sirtyler.pokedex.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GifDownloader {
    private static final File dir = new File(System.getProperty("user.dir"));
    private static final File save = new File(dir.getPath() + File.separator + "gifs");
    private static final String base = "http://play.pokemonshowdown.com/sprites/xyani/";
    private static final String base_shiny = "http://play.pokemonshowdown.com/sprites/xyani-shiny/";
    private static ExecutorService service = Executors.newSingleThreadExecutor();
    private DefaultListModel<String> listModel;
    private JPanel contentPane;
    private JProgressBar pkBar;
    private JCheckBox pkShiny;
    private JList pkList;
    private JButton pkOK;
    private String[] list;

    public static void main(String[] args) {
        GifDownloader gifDownloader = new GifDownloader();
        JFrame frame = new JFrame("Gif Downloader");
        frame.setContentPane(gifDownloader.contentPane);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 300, 300);
        frame.setVisible(true);
        SwingUtilities.invokeLater(gifDownloader::setUp);

        try {
            Document doc = Jsoup.connect(base).get();
            log(doc.title());

            Elements fList = doc.select("td a");
            fList.remove(0);

            String[] sList = new String[fList.size()];
            int i = 0;
            for (Element file : fList) {
                //log(file.attr("href"));
                sList[i++] = file.attr("href").replaceAll(".gif", "");
            }
            gifDownloader.addData(sList);

        } catch (Exception ex) {
            log("Error, Unable to Process");
            ex.printStackTrace();
        }
    }

    private static void log(String s) {
        System.out.println(s);
    }

    private void setUp() {
        pkOK.addActionListener(evt -> {
                    pkOK.setEnabled(false);

                    service.submit(() -> {
                        for (int i = 0; i < list.length; i++) {
                            try {
                                URL u = new URL((pkShiny.isSelected() ? base_shiny : base) + list[i] + ".gif");
                                InputStream in = u.openStream();
                                OutputStream out = new FileOutputStream(save.getPath() + File.separator + list[i] + ".gif");

                                in.transferTo(out);
                                out.flush();
                                in.close();
                                out.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            final int n = i;
                            EventQueue.invokeLater(() -> {
                                listModel.addElement(list[n]);
                                pkBar.setValue((n));
                            });

                        }

                        EventQueue.invokeLater(() -> pkOK.setEnabled(true));

                    });
                }
        );
    }

    public void addData(String[] inputArray) {
        listModel = new DefaultListModel<>();
        pkList.setModel(listModel);
        list = inputArray;
        pkBar.setMaximum(list.length);
        pkOK.setEnabled(true);
    }
}
