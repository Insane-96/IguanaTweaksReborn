package insane96mcp.iguanatweaksreborn.modules.sleeprespawn;

import insane96mcp.iguanatweaksreborn.base.ITModule;
import insane96mcp.iguanatweaksreborn.base.Label;
import insane96mcp.iguanatweaksreborn.modules.sleeprespawn.feature.DisableSleepingFeature;
import insane96mcp.iguanatweaksreborn.modules.sleeprespawn.feature.SleepingEffectsFeature;
import insane96mcp.iguanatweaksreborn.setup.Config;

@Label(name = "Sleep & Respawn")
public class SleepRespawnModule extends ITModule {

    public SleepingEffectsFeature sleepingEffectsFeature;
    public DisableSleepingFeature disableSleepingFeature;

    public SleepRespawnModule() {
        super();
        Config.builder.comment(this.getDescription()).push(this.getName());
        sleepingEffectsFeature = new SleepingEffectsFeature(this);
        disableSleepingFeature = new DisableSleepingFeature(this);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        sleepingEffectsFeature.loadConfig();
        disableSleepingFeature.loadConfig();
    }
}