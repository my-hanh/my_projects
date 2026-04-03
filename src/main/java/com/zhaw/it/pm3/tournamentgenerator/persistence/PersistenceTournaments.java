package com.zhaw.it.pm3.tournamentgenerator.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zhaw.it.pm3.tournamentgenerator.configurations.Config;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.TournamentMode;
import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import com.zhaw.it.pm3.tournamentgenerator.util.ObservableListTypeAdapter;
import com.zhaw.it.pm3.tournamentgenerator.util.TeamDeserializer;
import com.zhaw.it.pm3.tournamentgenerator.util.TournamentModeDeserializer;
import com.zhaw.it.pm3.tournamentgenerator.util.TournamentModeSerializer;
import javafx.collections.ObservableList;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for the persistence of the tournaments.
 */
public class PersistenceTournaments {

    private static String filePathJSON = "src/main/resources/tournament.json";

    private static Gson gson = new Gson();

    /**
     * Loads tournament configurations from a JSON file.
     *
     * @return a list of {@link Config} objects loaded from the JSON file. If the file does not exist or is empty,
     *         an empty list is returned.
     * @throws FileNotFoundException if the specified file path is invalid.
     */
    public static ArrayList<Config> loadConfigs() {
        File file = new File(filePathJSON);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(TournamentMode.class, new TournamentModeSerializer()).registerTypeAdapter(new TypeToken<ObservableList<Player>>() {
        }.getType(), new ObservableListTypeAdapter()).registerTypeAdapter(TournamentMode.class, new TournamentModeDeserializer()).registerTypeAdapter(Team.class, new TeamDeserializer()).create();

        try (FileReader reader = new FileReader(file)) {
            Type userListType = new TypeToken<List<Config>>() {
            }.getType();
            ArrayList<Config> configs = gson.fromJson(reader, userListType);
            return configs != null ? configs : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Saves a list of tournament configurations to a JSON file.
     *
     * @param config the list of {@link Config} objects to save to the JSON file.
     */
    public static void saveConfig(ArrayList<Config> config) {
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(TournamentMode.class, new TournamentModeSerializer()).registerTypeAdapter(new TypeToken<ObservableList<Player>>() {
        }.getType(), new ObservableListTypeAdapter()).registerTypeAdapter(TournamentMode.class, new TournamentModeDeserializer()).registerTypeAdapter(Team.class, new TeamDeserializer()).create();
        try (FileWriter writer = new FileWriter(filePathJSON)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a single tournament configuration to the JSON file.
     * Loads the existing configurations, appends the new configuration, and saves the updated list.
     *
     * @param config the {@link Config} object to add.
     * @throws FileNotFoundException if the JSON file cannot be loaded.
     */
    public static void addConfig(Config config) {
        ArrayList<Config> configs = loadConfigs();
        configs.add(config);
        saveConfig(configs);
    }
}