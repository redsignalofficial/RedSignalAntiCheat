package me.rin.redsignalanticheat.util.hook.plugin.simplehook;

import me.rin.redsignalanticheat.util.hook.plugin.HookUtil;

public class EliteMobsHook extends HookUtil {

    private static final String PLUGIN_NAME = "EliteMobs";

    public static boolean isPluginInstalled() {
        return isPlugin(PLUGIN_NAME);
    }

}
