package services.map;

import models.map.EffectMap;
import models.map.Map;
import models.map.Zone;
import models.player.Player;
import network.io.Message;
import server.Manager;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamic Weather & Environmental Atmosphere Engine
 * Synchronizes real-time weather and lighting effects across all maps without breaking legacy clients.
 */
public class WeatherService {

    private static WeatherService instance;

    // MapId -> List of active weather effects
    private final java.util.Map<Integer, List<EffectMap>> activeWeatherMap = new ConcurrentHashMap<>();

    public static WeatherService gI() {
        if (instance == null) {
            instance = new WeatherService();
        }
        return instance;
    }

    private WeatherService() {
    }

    public void initWeatherForMap(Map map) {
        if (map == null) {
            return;
        }
        List<EffectMap> effects = new ArrayList<>();
        if (map.effMap != null) {
            effects.addAll(map.effMap);
        }

        // Apply default ambient atmospheric presets if not already specified
        switch (map.mapId) {
            case 5: // Đảo Kame - Rainy ambient
                addEffectIfNotExists(effects, "beff", "0");
                addEffectIfNotExists(effects, "beff", "12");
                break;
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110: // Thành phố Cold / Băng tuyết - Snowfall
                addEffectIfNotExists(effects, "beff", "1");
                break;
            case 131:
            case 132:
            case 133: // Núi lửa Fide - Lava ember
                addEffectIfNotExists(effects, "beff", "2");
                break;
        }

        map.effMap = effects;
        activeWeatherMap.put(map.mapId, effects);
    }

    private void addEffectIfNotExists(List<EffectMap> list, String key, String value) {
        for (EffectMap em : list) {
            if (em.getKey().equals(key) && em.getValue().equals(value)) {
                return;
            }
        }
        EffectMap newEm = new EffectMap();
        newEm.setKey(key);
        newEm.setValue(value);
        list.add(newEm);
    }

    public void setWeather(int mapId, String key, String value) {
        Map map = MapService.gI().getMapById(mapId);
        if (map != null) {
            if (map.effMap == null) {
                map.effMap = new ArrayList<>();
            }
            // Remove existing key
            map.effMap.removeIf(e -> e.getKey().equals(key));
            if (value != null && !value.isEmpty()) {
                EffectMap em = new EffectMap();
                em.setKey(key);
                em.setValue(value);
                map.effMap.add(em);
            }
            activeWeatherMap.put(mapId, map.effMap);
            broadcastWeatherToMap(map);
        }
    }

    public void broadcastWeatherToMap(Map map) {
        if (map == null || map.zones == null) {
            return;
        }
        for (Zone zone : map.zones) {
            if (zone != null) {
                List<Player> players = zone.getPlayers();
                for (Player pl : players) {
                    if (pl != null && pl.isPl()) {
                        // Re-send mapInfo or weather effect update
                        zone.mapInfo(pl);
                    }
                }
            }
        }
    }
}
