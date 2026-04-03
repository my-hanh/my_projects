package com.zhaw.it.pm3.tournamentgenerator.util;
import com.google.gson.*;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.RoundRobin;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.SingleElimination;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.Swiss;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.TournamentMode;

import java.lang.reflect.Type;


/**
 * Serializes TournamentMode objects into JSON.
 */
public class TournamentModeSerializer implements JsonSerializer<TournamentMode>{

    /**
     * Converts a TournamentMode into a JSON string.
     *
     * @param src the TournamentMode to serialize
     * @param typeOfSrc the type of the object being serialized
     * @param context the serialization context
     * @return a JSON element representing the TournamentMode
     */
    @Override
    public JsonElement serialize(TournamentMode src, Type typeOfSrc, JsonSerializationContext context) {
        if (src instanceof SingleElimination) {
                return new JsonPrimitive("Single Elimination");
            } else if (src instanceof RoundRobin) {
            return new JsonPrimitive("Round Robin");
        } else if (src instanceof Swiss) {
            return new JsonPrimitive("Swiss");
        }
        // Handle other TournamentModes if needed
        return JsonNull.INSTANCE;
    }
}