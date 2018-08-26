package me.sirtyler.pokedex;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceDirectory;
import com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory;
import me.sargunvohra.lib.pokekotlin.client.PokeApi;
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient;
import me.sirtyler.pokedex.gui.PokedexFrame;
import me.sirtyler.pokedex.tools.DBConnector;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JPokedex {

    public static JPokedex instance;
    public static PokeApi pokeApi;
    private static Voice voice;
    public DBConnector db;
    private JFrame frame;
    private PokedexFrame pkdexFrame;
    private HashMap<Integer, ArrayList<JPokemon>> map_poke = new HashMap<>();
    private ArrayList<JPokemon> list_poke = new ArrayList<>();
    private HashMap<String, String> phon_map = new HashMap<>();

    public JPokedex() {
        instance = this;
        pokeApi = new PokeApiClient();

        pkdexFrame = new PokedexFrame();
        frame = new JFrame("Pokedex");
        frame.setContentPane(pkdexFrame.contentPane);
        frame.setResizable(false);
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(PokedexFrame.class.getResource("/res/icon.png")));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 600, 800);
        frame.setVisible(true);

        VoiceDirectory voiceDirectory = new KevinVoiceDirectory();
        voice = voiceDirectory.getVoices()[1];
        if (voice != null) {
            voice.setVolume(0.9f);
            voice.setPitch(160f);
            voice.setRate(175f);
            voice.allocate();
        } else {
            System.out.println("ERROR: Voice");
        }

        db = new DBConnector();
        db.open();
        db.load(map_poke, list_poke, phon_map);

        pkdexFrame.setup();
    }

    public static void main(String[] args) {
        new JPokedex();
    }

    public void dispose() {
        db.close();
        voice.endBatch();
        voice.deallocate();
    }

    public JPokemon getPokemon(String name) {
        for (Map.Entry<Integer, ArrayList<JPokemon>> entry : map_poke.entrySet()) {
            int key = entry.getKey();
            ArrayList<JPokemon> value = entry.getValue();
            for (JPokemon p : value) {
                if (p.name.equalsIgnoreCase(name.replaceAll(" ", "-"))) {
                    return p;
                }
            }
        }
        return null;
    }

    public String translate(String sentence) {
        String ss = sentence;
        for (Map.Entry<String, String> entry : phon_map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            ss = ss.replaceAll(key, value);
        }
        return ss;
    }

    public String getPhon(String name) {
        if (phon_map.containsKey(name.toUpperCase()))
            return phon_map.get(name);
        else return name;
    }

    public HashMap<Integer, ArrayList<JPokemon>> getMap() {
        return (HashMap<Integer, ArrayList<JPokemon>>) map_poke.clone();
    }

    public ArrayList<JPokemon> getList() {
        return (ArrayList<JPokemon>) list_poke.clone();
    }

    public void speak(String text) {
        Thread t = new Thread(() -> voice.speak(text));
        t.start();
    }
}