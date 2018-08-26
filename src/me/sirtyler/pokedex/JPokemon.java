package me.sirtyler.pokedex;

import me.sargunvohra.lib.pokekotlin.model.*;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static me.sirtyler.pokedex.JPokedex.pokeApi;

public class JPokemon {
    public int id, id_dex;
    public String name;
    public String phon;
    public String type;
    public String cat;
    public String desc;
    public int b_HP, b_ATK, b_DEF, b_SPATK, b_SPDEF, b_SPEED;
    public String sprite;
    public URL icon;
    public String voice_read;

    public static List<JPokemon> build(Pokemon poke) {
        ArrayList<JPokemon> list = new ArrayList<>();

        PokemonSpecies pkSpec = pokeApi.getPokemonSpecies(poke.getId());
        List<PokemonSpeciesVariety> very = pkSpec.getVarieties();
        for (PokemonSpeciesVariety v : very) {
            int i = v.getPokemon().getId();
            list.add(JPokemon.build_sub(pokeApi.getPokemon(i), pkSpec));
        }

        return list;
    }

    private static JPokemon build_sub(Pokemon poke, PokemonSpecies pkSpec) {
        JPokemon pk = new JPokemon();
        System.out.println("Building Pokemon " + poke.getId());

        pk.id = poke.getId();
        pk.id_dex = pkSpec.getId();
        String name = poke.getName();
        pk.name = name.substring(0, 1).toUpperCase() + name.substring(1);

        List<PokemonType> types = poke.getTypes();
        String ss;
        if (types.size() > 1) {
            PokemonType a = types.get(0);
            PokemonType b = types.get(1);
            String aa = a.getType().component1();
            aa = aa.substring(0, 1).toUpperCase() + aa.substring(1);
            String bb = b.getType().component1();
            bb = bb.substring(0, 1).toUpperCase() + bb.substring(1);

            if (a.getSlot() > 1) {
                ss = bb + "/" + aa;
            } else {
                ss = aa + "/" + bb;
            }

        } else {
            String aa = types.get(0).getType().component1();
            ss = aa.substring(0, 1).toUpperCase() + aa.substring(1);
        }
        pk.type = ss;
        pk.cat = pkSpec.getGenera().get(2).getGenus().replaceAll("Pokémon", "").trim();

        List<PokemonSpeciesFlavorText> flavors = pkSpec.getFlavorTextEntries();
        String flavor = "";
        for (PokemonSpeciesFlavorText f : flavors) {
            if (f.getLanguage().component3() == 9) {
                flavor = f.getFlavorText();
                flavor = flavor.replace("\n", " ").replace("\r", "");
                flavor = flavor.replaceAll("Pokémon", "Pokemon");
                break;
            }
        }

        pk.desc = flavor;

        pk.b_SPEED = poke.getStats().get(0).getBaseStat();
        pk.b_SPDEF = poke.getStats().get(1).getBaseStat();
        pk.b_SPATK = poke.getStats().get(2).getBaseStat();
        pk.b_DEF = poke.getStats().get(3).getBaseStat();
        pk.b_ATK = poke.getStats().get(4).getBaseStat();
        pk.b_HP = poke.getStats().get(5).getBaseStat();

        try {
            //pk.icon = new URL(String.format("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%s.png", pk.id));
            String n = pk.name;
            /*if (pk.name.endsWith("-average")) n = n.replaceAll("-average", "");
            else if (pk.name.endsWith("-shield")) n = n.replaceAll("-shield", "");
            else if (pk.name.endsWith("-male")) n = n.replaceAll("-male", "");
            else if (pk.name.endsWith("-f") || name.endsWith("-m"))
                n = name.replaceAll("-f", "f").replaceAll("-m", "m");
            else n = n.replaceAll("mega-x", "megax").replaceAll("mega-y", "megay");*/
            pk.icon = JPokedex.class.getResource("/gifs/" + name + ".gif");
            //pk.icon_ani = new URL(String.format("http://play.pokemonshowdown.com/sprites/xyani/%s.gif", n.toLowerCase().trim()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pk;
    }

    public void load(ResultSet rs) throws SQLException {
        id = rs.getInt("ID");
        id_dex = rs.getInt("ID_DEX");
        name = rs.getString("NAME");
        phon = rs.getString("PHON");
        type = rs.getString("TYPE");
        cat = rs.getString("CAT").trim();
        desc = rs.getString("DESC");
        b_HP = rs.getInt("BASE_HP");
        b_ATK = rs.getInt("BASE_ATK");
        b_DEF = rs.getInt("BASE_DEF");
        b_SPATK = rs.getInt("BASE_SPATK");
        b_SPDEF = rs.getInt("BASE_SPDEF");
        b_SPEED = rs.getInt("BASE_SPEED");
        sprite = rs.getString("SPRITE");
        try {
            if (sprite == null || sprite.equals("")) {
                String n = name.toLowerCase();
                if (n.endsWith("-average")) n = n.replaceAll("-average", "");
                else if (n.endsWith("-shield")) n = n.replaceAll("-shield", "");
                else if (n.endsWith("-male")) n = n.replaceAll("-male", "");
                else if (n.endsWith("-f") || name.endsWith("-m")) n = name.replaceAll("-f", "f").replaceAll("-m", "m");
                else n = n.replaceAll("mega-x", "megax").replaceAll("mega-y", "megay");
                sprite = n;
            }
            icon = JPokedex.class.getResource("/gifs/" + sprite + ".gif");
        } catch (Exception e) {
            e.printStackTrace();
        }

        voice_read = String.format("%s; The %s Type Pokemon. %s", name, cat, desc).replaceAll("\'s", "s");
        if (phon != null)
            voice_read = voice_read.replaceAll(name, phon);
        //System.out.println("Loaded "+name);
    }

    @Override
    public String toString() {
        return ("#" + id_dex + " - " + name);
    }
}
