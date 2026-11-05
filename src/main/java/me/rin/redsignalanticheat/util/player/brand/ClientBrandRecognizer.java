package me.rin.redsignalanticheat.util.player.brand;

import me.rin.redsignalanticheat.util.hook.server.paper.PaperUtil;
import me.rin.redsignalanticheat.util.reflection.ReflectionException;
import me.rin.redsignalanticheat.util.reflection.ReflectionUtil;
import org.bukkit.entity.Player;

public class ClientBrandRecognizer {

    public static String getClientBrand(Player player) {
        Object clientBrandObject = null;
        if (PaperUtil.isPaper()) {
            try {
                clientBrandObject = ReflectionUtil.runDeclaredMethod(player, "getClientBrandName");
            } catch (ReflectionException ignored) {
            }
        }
        String clientBrand = clientBrandObject instanceof String ? clientBrandObject.toString() : "unknown";
        if (clientBrand.length() >= 1)
            clientBrand = clientBrand.substring(0, 1).toUpperCase() + clientBrand.substring(1);
        return clientBrand;
    }

}
