/*package ru.yuzu.antivpn;


import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

public class Antivpn {

    @Inject private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}

*/

package ru.yuzu.antivpn;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Plugin(id = "antivpn", name = "AntiVPN", version = "1.0",
        url = "https://microhost1.ru", description = "SLAVA ROSSII!", authors = {"Novadev"})

public class Antivpn {
    @Inject
    private ProxyServer proxyServer;
    @Inject
    private IPWhitelistStorage whitelistStorage;
    @Inject private Logger logger;
    private Cache<String, Double> cache = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.DAYS).build();
    IPWhitelist whitelist;

    @Subscribe
    public void ProxyInitializeEvent(ProxyInitializeEvent event) {
        whitelist = whitelistStorage.readWhitelist();
    }

    @Subscribe
    public void ServerPreConnectEvent(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        String ip = player.getRemoteAddress().getAddress().getHostAddress();
       // logger.info("Users IP is " + ip + " whitelisted are " + whitelist.ips);
       // logger.info("Whitelisted: "+ whitelist.ips.contains(ip));


        if(player.hasPermission("antivpn.bypass")) {
            return;
        }
        if(whitelist.ips.contains(ip)) {
            return;
        }
        // Check the IP score using the getipintel.net API
        double ipScore = requestIPScore(ip);

        if (ipScore > 0.98) {
            // Kick the player if their IP score is greater than 0.98 and they don't have the antivpn.bypass permission
            player.disconnect(Component.text("§a[Сбербан(к) AntiVPN]\n§4Обнаружен ВПН."));

            // Notify all players with the antivpn.notify permission about the kicked player
            String message = "§a[Сбербан(к) AntiVPN] §fPlayer §6" + player.getUsername() + "§f with IP address §6" + ip + "§f and score §4" + ipScore + "§f was kicked for using a proxy or VPN.";
            proxyServer.getAllPlayers().forEach(onlinePlayer -> {
                if (onlinePlayer.hasPermission("antivpn.notify")) {
                    onlinePlayer.sendMessage(Component.text(message));
                }
            });
        }
    }

    private double getIPScore(String ip) {
        Double cachedScore = cache.getIfPresent(ip);
        if(cachedScore == null) {
            cachedScore = requestIPScore(ip);
            cache.put(ip,cachedScore);
        }
        return cachedScore;
    }
    private double requestIPScore(String ip) {
        try {
            // Set up the API request
            URL url = new URL("https://check.getipintel.net/check.php?ip=" + ip + "&contact=roland@scheffler.software");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            // Read the response from the API
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Parse the response and return the IP score
            return Double.parseDouble(content.toString());
        } catch (Exception e) {
            // If there is an error, log it and return a score of 0
            throw new java.lang.Error("Error checking IP score: " + e.getMessage());
        }
    }
}
