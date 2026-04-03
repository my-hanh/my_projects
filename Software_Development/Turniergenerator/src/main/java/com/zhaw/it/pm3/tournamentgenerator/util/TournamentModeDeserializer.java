package com.zhaw.it.pm3.tournamentgenerator.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.RoundRobin;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.SingleElimination;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.Swiss;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.TournamentMode;

import java.lang.reflect.Type;

/**
 * Deserializes JSON strings into TournamentMode objects.
 */
public class TournamentModeDeserializer implements JsonDeserializer<TournamentMode> {

    /**
     * Converts a JSON string into a TournamentMode object.
     *
     * @param json the JSON element to deserialize
     * @param typeOfT the type of the object being deserialized
     * @param context the deserialization context
     * @return the deserialized TournamentMode object
     * @throws JsonParseException if the JSON string does not match a known TournamentMode
     */
    @Override
    public TournamentMode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String mode = json.getAsString();

        return switch (mode) {
            case "Single Elimination" -> new SingleElimination();
            case "Round Robin" -> new RoundRobin();
            case "Swiss" -> new Swiss();
            default -> throw new JsonParseException("Unknown tournament mode: " + mode);
        };
    }
}
