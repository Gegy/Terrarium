package net.gegy1000.terrarium.server.world.pipeline.source;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.json.ParseStateHandler;
import net.gegy1000.terrarium.server.world.json.ParseUtils;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MappedGeocoder implements Geocoder {
    private final TreeMap<String, Coordinate> coordinateMap;

    public MappedGeocoder(TreeMap<String, Coordinate> coordinateMap) {
        this.coordinateMap = coordinateMap;
    }

    @Override
    public Coordinate get(String place) {
        return this.coordinateMap.get(place.trim());
    }

    @Override
    public String[] suggest(String place, boolean command) {
        if (command) {
            return this.coordinateMap.keySet().toArray(new String[0]);
        }

        place = place.toLowerCase(Locale.ROOT).trim();

        List<String> suggestions = new LinkedList<>();
        for (String key : this.coordinateMap.keySet()) {
            if (key.toLowerCase(Locale.ROOT).startsWith(place)) {
                suggestions.add(key);
            }
        }

        return suggestions.toArray(new String[0]);
    }

    public static class Parser implements InstanceObjectParser<Geocoder> {
        @Override
        public Geocoder parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            CoordinateState coordinateState = valueParser.parseCoordinateState(objectRoot, "coordinate_state");
            TreeMap<String, Coordinate> coordinateMap = ParseUtils.parseObject(objectRoot, "coordinates", coordinateRoot -> {
                TreeMap<String, Coordinate> coords = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                for (Map.Entry<String, JsonElement> entry : coordinateRoot.entrySet()) {
                    String key = entry.getKey();
                    if (entry.getValue().isJsonObject()) {
                        JsonObject coordinateObject = entry.getValue().getAsJsonObject();
                        try {
                            double x = valueParser.parseDouble(coordinateObject, "x");
                            double z = valueParser.parseDouble(coordinateObject, "z");
                            coords.put(key.trim(), new Coordinate(coordinateState, x, z));
                        } catch (InvalidJsonException e) {
                            ParseStateHandler.error(e);
                        }
                    } else {
                        ParseStateHandler.error("Found invalid coordinate mapping element for \"" + key + "\"");
                    }
                }
                return coords;
            });

            return new MappedGeocoder(coordinateMap);
        }
    }
}
