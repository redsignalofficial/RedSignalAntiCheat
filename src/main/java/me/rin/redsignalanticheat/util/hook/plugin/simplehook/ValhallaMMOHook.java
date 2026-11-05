package me.rin.redsignalanticheat.util.hook.plugin.simplehook;

import me.rin.redsignalanticheat.util.hook.plugin.HookUtil;

public class ValhallaMMOHook extends HookUtil {

    private static final String PLUGIN_NAME = "ValhallaMMO";

    public static boolean isPluginInstalled() {
        return isPlugin(PLUGIN_NAME);
    }

}
