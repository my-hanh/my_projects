package com.zhaw.it.pm3.tournamentgenerator.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Deserializes JSON objects into Team objects.
 */
public class TeamDeserializer implements JsonDeserializer<Team> {

    /**
     * Converts a JSON object into a Team object.
     *
     * @param json the JSON element to deserialize
     * @param typeOfT the type of the object being deserialized
     * @param context the deserialization context
     * @return the deserialized Team object
     * @throws JsonParseException if the JSON is invalid or cannot be parsed
     */
    @Override
    public Team deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String name = jsonObject.get("name").getAsString();
        int points = jsonObject.get("points").getAsInt();

        // Deserialize players as ArrayList and convert to ObservableList
        ArrayList<Player> playersList = context.deserialize(jsonObject.get("players"), new TypeToken<ArrayList<Player>>() {}.getType());
        ObservableList<Player> playersObservableList = FXCollections.observableArrayList(playersList);

        // Create Team object and set fields
        Team team = new Team(name);
        team.setPoints(points);
        team.setPlayers(playersObservableList);

        return team;
    }
}
