package com.zhaw.it.pm3.tournamentgenerator.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zhaw.it.pm3.tournamentgenerator.persons.Administrator;
import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.util.ObservableListTypeAdapter;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for managing the data of administrators.
 * It provides methods to load, save, add, update, and validate administrator data.
 */
public class UserDataManager {

    private static final String USERS_FILE = "src/main/resources/users.json";
    private static final Gson gson = new Gson();

    /**
     * Loads the list of all administrators from the JSON file.
     * If the file does not exist or is empty, returns an empty list.
     *
     * @return a list of {@link Administrator} objects representing all saved administrators.
     */
    public static List<Administrator> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        // Use the Gson with the registered adapter
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<ObservableList<Player>>() {}.getType(), new ObservableListTypeAdapter())
                .create();

        try (FileReader reader = new FileReader(file)) {
            Type userListType = new TypeToken<List<Administrator>>() {}.getType();
            List<Administrator> admins = gson.fromJson(reader, userListType);
            return admins != null ? admins : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Saves the given list of administrators to the JSON file.
     * If the file does not exist, it will be created.
     *
     * @param users the list of {@link Administrator} objects to save.
     */
    public static void saveUsers(List<Administrator> users) {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new administrator or updates an existing one in the JSON file.
     * If an administrator with the same username exists, their data is updated.
     * Otherwise, the new administrator is added to the list.
     *
     * @param admin the {@link Administrator} object to add or update.
     */
    public static void addOrUpdateUser(Administrator admin) {
        List<Administrator> users = loadUsers();

        boolean userFound = false;
        for (Administrator u : users) {
            if (u.getName().equals(admin.getName())) {
                u.setPassword(admin.getPassword());
                u.setSenderEmail(admin.getSenderEmail());
                u.setSenderPassword(admin.getSenderPassword());
                userFound = true;
                break;
            }
        }

        if (!userFound) {
            users.add(admin);
        }

        saveUsers(users);
    }

    /**
     * Validates the credentials of an administrator by checking the JSON file.
     * Compares the provided username and password with the saved data.
     *
     * @param username the username of the administrator.
     * @param password the password of the administrator.
     * @return {@code true} if the credentials are valid, {@code false} otherwise.
     */
    public static boolean validateCredentials(String username, String password) {
        List<Administrator> users = loadUsers();
        return users.stream().anyMatch(u -> u.getName().equals(username) && u.getPassword().equals(password));
    }
}
