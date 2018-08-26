package me.sirtyler.pokedex.tools;

import me.sirtyler.pokedex.JPokemon;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DBConnector {
    Connection c = null;

    public void open() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:assets/pokedex.db");
            c.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public void close() {
        try {
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void forceWrite() {
        try {
            c.commit();
            System.out.println("Force Save");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void load(HashMap<Integer, ArrayList<JPokemon>> map, ArrayList<JPokemon> list, HashMap<String, String> phons) {
        ResultSet rs;

        try {
            Statement stmt = c.createStatement();
            rs = stmt.executeQuery("SELECT * FROM pokemon ORDER BY ID_DEX");

            while (rs.next()) {
                JPokemon p = new JPokemon();
                p.load(rs);

                ArrayList<JPokemon> l = new ArrayList<>();
                if (map.containsKey(p.id_dex)) l = map.get(p.id_dex);
                else list.add(p);

                l.add(p);
                map.put(p.id_dex, l);
                if (phons != null) phons.put(p.name.toUpperCase(), p.phon.toUpperCase());
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println("Operation done successfully");
    }

    public void insert(JPokemon pokemon) {
        try {
            String sql = "INSERT INTO pokemon(ID, ID_DEX, NAME, TYPE, CAT, DESC, BASE_HP, BASE_ATK, BASE_DEF, BASE_SPATK, BASE_SPDEF, BASE_SPEED) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setInt(1, pokemon.id);
            stmt.setInt(2, pokemon.id_dex);
            stmt.setString(3, pokemon.name);
            stmt.setString(4, pokemon.type);
            stmt.setString(5, pokemon.cat);
            stmt.setString(6, pokemon.desc);
            stmt.setInt(7, pokemon.b_HP);
            stmt.setInt(8, pokemon.b_ATK);
            stmt.setInt(9, pokemon.b_DEF);
            stmt.setInt(10, pokemon.b_SPATK);
            stmt.setInt(11, pokemon.b_SPDEF);
            stmt.setInt(12, pokemon.b_SPEED);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        //System.out.println("Saved "+pokemon.name+" successfully");
    }

    public void update(JPokemon pokemon) {
        try {
            String sql = "UPDATE pokemon SET BASE_HP = ?, BASE_ATK = ?, BASE_DEF = ?, BASE_SPATK = ?, BASE_SPDEF = ?, BASE_SPEED = ? , SPRITE = ? WHERE name = ?";
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setInt(1, pokemon.b_HP);
            stmt.setInt(2, pokemon.b_ATK);
            stmt.setInt(3, pokemon.b_DEF);
            stmt.setInt(4, pokemon.b_SPATK);
            stmt.setInt(5, pokemon.b_SPDEF);
            stmt.setInt(6, pokemon.b_SPEED);
            stmt.setString(7, pokemon.name);
            stmt.setString(8, pokemon.sprite);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println(pokemon.sprite);
        System.out.println("Updated " + pokemon.name + " successfully");
    }

    public void addPhonetic(JPokemon pokemon) {
        try {
            String sql = "UPDATE pokemon SET PHON = ? WHERE name = ?";
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, pokemon.phon);
            stmt.setString(2, pokemon.name);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        //System.out.println("Updated "+pokemon.name+" successfully");
    }
}
