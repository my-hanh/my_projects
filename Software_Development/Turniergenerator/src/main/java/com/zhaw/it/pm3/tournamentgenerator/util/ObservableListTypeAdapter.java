package com.zhaw.it.pm3.tournamentgenerator.util;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.List;

/**
 * Handles serialization and deserialization of ObservableList<Player> objects.
 */
public class ObservableListTypeAdapter extends TypeAdapter<ObservableList<Player>> {

    private final Gson gson = new Gson();

    private final TypeToken<List<Player>> listTypeToken = new TypeToken<List<Player>>() {};

    /**
     * Serializes an ObservableList<Player> to JSON.
     *
     * @param out   the JsonWriter to write to
     * @param value the ObservableList<Player> to serialize
     * @throws IOException if an error occurs during writing
     */
    @Override
    public void write(JsonWriter out, ObservableList<Player> value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        // Convert ObservableList to a normal List for serialization
        List<Player> list = List.copyOf(value);
        gson.toJson(list, listTypeToken.getType(), out);
    }

    /**
     * Deserializes a JSON array into an ObservableList<Player>.
     *
     * @param in the JsonReader to read from
     * @return the deserialized ObservableList<Player>
     * @throws IOException if an error occurs during reading
     */
    @Override
    public ObservableList<Player> read(JsonReader in) throws IOException {
        // Deserialize JSON array into a regular List first
        List<Player> list = gson.fromJson(in, listTypeToken.getType());
        // Convert to ObservableList
        return FXCollections.observableArrayList(list);
    }
}

