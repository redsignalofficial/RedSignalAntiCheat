package me.rin.redsignalanticheat;

import me.rin.redsignalanticheat.check.Check;
import me.rin.redsignalanticheat.check.buffer.Buffer;
import me.rin.redsignalanticheat.check.checks.combat.autoclicker.AutoClickerA;
import me.rin.redsignalanticheat.check.checks.combat.autoclicker.AutoClickerB;
import me.rin.redsignalanticheat.check.checks.combat.criticals.CriticalsA;
import me.rin.redsignalanticheat.check.checks.combat.criticals.CriticalsB;
import me.rin.redsignalanticheat.check.checks.combat.killaura.*;
import me.rin.redsignalanticheat.check.checks.combat.reach.ReachA;
import me.rin.redsignalanticheat.check.checks.combat.reach.ReachB;
import me.rin.redsignalanticheat.check.checks.combat.velocity.VelocityA;
import me.rin.redsignalanticheat.check.checks.interaction.airplace.AirPlaceA;
import me.rin.redsignalanticheat.check.checks.interaction.blockbreak.BlockBreakA;
import me.rin.redsignalanticheat.check.checks.interaction.blockbreak.BlockBreakB;
import me.rin.redsignalanticheat.check.checks.interaction.blockplace.BlockPlaceA;
import me.rin.redsignalanticheat.check.checks.interaction.blockplace.BlockPlaceB;
import me.rin.redsignalanticheat.check.checks.interaction.fastbreak.FastBreakA;
import me.rin.redsignalanticheat.check.checks.interaction.fastplace.FastPlaceA;
import me.rin.redsignalanticheat.check.checks.interaction.ghostbreak.GhostBreakA;
import me.rin.redsignalanticheat.check.checks.interaction.scaffold.ScaffoldA;
import me.rin.redsignalanticheat.check.checks.interaction.scaffold.ScaffoldB;
import me.rin.redsignalanticheat.check.checks.inventory.sorting.SortingA;
import me.rin.redsignalanticheat.check.checks.inventory.swapping.ItemSwapA;
import me.rin.redsignalanticheat.check.checks.movement.boat.BoatA;
import me.rin.redsignalanticheat.check.checks.movement.elytra.ElytraA;
import me.rin.redsignalanticheat.check.checks.movement.elytra.ElytraB;
import me.rin.redsignalanticheat.check.checks.movement.elytra.ElytraC;
import me.rin.redsignalanticheat.check.checks.movement.fastclimb.FastClimbA;
import me.rin.redsignalanticheat.check.checks.movement.flight.*;
import me.rin.redsignalanticheat.check.checks.movement.jump.JumpA;
import me.rin.redsignalanticheat.check.checks.movement.jump.JumpB;
import me.rin.redsignalanticheat.check.checks.movement.liquidwalk.LiquidWalkA;
import me.rin.redsignalanticheat.check.checks.movement.liquidwalk.LiquidWalkB;
import me.rin.redsignalanticheat.check.checks.movement.nofall.NoFallA;
import me.rin.redsignalanticheat.check.checks.movement.nofall.NoFallB;
import me.rin.redsignalanticheat.check.checks.movement.noslow.NoSlowA;
import me.rin.redsignalanticheat.check.checks.movement.speed.*;
import me.rin.redsignalanticheat.check.checks.movement.step.StepA;
import me.rin.redsignalanticheat.check.checks.movement.trident.TridentA;
import me.rin.redsignalanticheat.check.checks.movement.vehicle.VehicleA;
import me.rin.redsignalanticheat.check.checks.packet.badpackets.BadPacketsA;
import me.rin.redsignalanticheat.check.checks.packet.badpackets.BadPacketsB;
import me.rin.redsignalanticheat.check.checks.packet.badpackets.BadPacketsC;
import me.rin.redsignalanticheat.check.checks.packet.badpackets.BadPacketsD;
import me.rin.redsignalanticheat.check.checks.packet.morepackets.MorePacketsA;
import me.rin.redsignalanticheat.check.checks.packet.morepackets.MorePacketsB;
import me.rin.redsignalanticheat.check.checks.packet.timer.TimerA;
import me.rin.redsignalanticheat.check.checks.packet.timer.TimerB;
import me.rin.redsignalanticheat.check.checks.player.autobot.AutoBotA;
import me.rin.redsignalanticheat.check.checks.player.skinblinker.SkinBlinkerA;
import me.rin.redsignalanticheat.command.LACCommand;
import me.rin.redsignalanticheat.event.LACEventCaller;
import me.rin.redsignalanticheat.listener.invalidping.InvalidPingListener;
import me.rin.redsignalanticheat.listener.unloadedchunk.UnloadedChunkListener;
import me.rin.redsignalanticheat.player.LACPlayerListener;
import me.rin.redsignalanticheat.util.api.ApiUtil;
import me.rin.redsignalanticheat.util.config.ConfigManager;
import me.rin.redsignalanticheat.util.hook.server.folia.FoliaUtil;
import me.rin.redsignalanticheat.util.logger.Logger;
import me.rin.redsignalanticheat.util.npc.ExternalNPCUtil;
import me.rin.redsignalanticheat.util.player.connectionstability.ConnectionStabilityListener;
import me.rin.redsignalanticheat.util.player.cps.CPSListener;
import me.rin.redsignalanticheat.util.tps.TPSCalculator;
import me.rin.redsignalanticheat.util.updater.Updater;
import me.rin.redsignalanticheat.util.violation.ViolationHandler;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private static LACEventCaller eventCaller;
    private static final long BUFFER_DURATION_MILS = 20 * 1000L;
    private static final int PLUGIN_ID = 112053;
    private static final int STATS_ID = 12841;

    @Override
    public void onEnable() {
        instance = this;
        FoliaUtil.loadFoliaUtil();
        ConfigManager.loadConfig();

        Buffer.loadBufferCleaner(BUFFER_DURATION_MILS);
        TPSCalculator.loadTPSCalculator();
        Logger.logFile("");
        ApiUtil.setApiInstance();

        LACPlayerListener.loadLACPlayerListener();
        registerListener(new LACPlayerListener());

        ExternalNPCUtil.loadExternalNPCUtil();

        registerListener(new ViolationHandler());

        eventCaller = new LACEventCaller();
        registerListener(eventCaller);

        UnloadedChunkListener.handleUnloadedChunks();
        InvalidPingListener.limitMaxPing();

        CPSListener.loadCpsCalculatorOnReload();
        CPSListener.loadCpsCalculator();
        registerListener(new CPSListener());
        ConnectionStabilityListener.loadConnectionCalculatorOnReload();
        ConnectionStabilityListener.loadConnectionCalculator();
        registerListener(new ConnectionStabilityListener());

        PluginCommand antiCheatCommand = getCommand("redsignalanticheat");
        if (antiCheatCommand != null) {
            antiCheatCommand.setExecutor(new LACCommand());
            antiCheatCommand.setTabCompleter(new LACCommand());
        }

        Updater.loadUpdateChecker();
        registerListener(new Updater());

        registerCheckListener(new FlightA());
        registerCheckListener(new FlightB());
        registerCheckListener(new FlightC());
        registerCheckListener(new FlightC());
        registerCheckListener(new LiquidWalkA());
        registerCheckListener(new LiquidWalkB());
        registerCheckListener(new JumpA());
        registerCheckListener(new JumpB());
        registerCheckListener(new ElytraA());
        registerCheckListener(new ElytraB());
        registerCheckListener(new ElytraC());
        registerCheckListener(new FastClimbA());
        registerCheckListener(new NoFallA());
        registerCheckListener(new NoFallB());
        registerCheckListener(new NoSlowA());
        registerCheckListener(new SpeedA());
        registerCheckListener(new SpeedB());
        registerCheckListener(new SpeedD());
        registerCheckListener(new SpeedE());
        registerCheckListener(new SpeedE());
        registerCheckListener(new SpeedF());
        registerCheckListener(new SpeedC());
        registerCheckListener(new StepA());
        registerCheckListener(new TridentA());
        registerCheckListener(new BoatA());
        registerCheckListener(new VehicleA());
        registerCheckListener(new KillAuraA());
        registerCheckListener(new KillAuraB());
        registerCheckListener(new KillAuraC());
        registerCheckListener(new KillAuraD());
        registerCheckListener(new ReachA());
        registerCheckListener(new ReachB());
        registerCheckListener(new CriticalsA());
        registerCheckListener(new CriticalsB());
        registerCheckListener(new AutoClickerA());
        registerCheckListener(new AutoClickerB());
        registerCheckListener(new VelocityA());
        registerCheckListener(new AirPlaceA());
        registerCheckListener(new FastPlaceA());
        registerCheckListener(new BlockPlaceA());
        registerCheckListener(new BlockPlaceB());
        registerCheckListener(new GhostBreakA());
        registerCheckListener(new FastBreakA());
        registerCheckListener(new BlockBreakA());
        registerCheckListener(new BlockBreakB());
        registerCheckListener(new ScaffoldA());
        registerCheckListener(new ScaffoldB());
        registerCheckListener(new SortingA());
        registerCheckListener(new ItemSwapA());
        registerCheckListener(new MorePacketsA());
        registerCheckListener(new MorePacketsB());
        registerCheckListener(new TimerA());
        registerCheckListener(new TimerB());
        registerCheckListener(new BadPacketsA());
        registerCheckListener(new BadPacketsB());
        registerCheckListener(new BadPacketsC());
        registerCheckListener(new BadPacketsD());
        registerCheckListener(new AutoBotA());
        registerCheckListener(new SkinBlinkerA());
    }

    @Override
    public void onDisable() {
        if (eventCaller != null)
            eventCaller.close();
    }

    public static Main getInstance() {
        return instance;
    }

    public static long getBufferDurationMils() {
        return BUFFER_DURATION_MILS;
    }

    public static int getPluginId() {
        return PLUGIN_ID;
    }

    public static int getStatsId() {
        return STATS_ID;
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCheckListener(Object object) {
        Check.registerListener(((Check) object).getCheckSetting().name, (Listener) object);
    }

}
