package grcmcs.minecraft.mods.pomkotsmechs.forge.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.gui.LostCitySetup;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
    static {
        PomkotsMechs.LOGGER.info("MIXIN CreateWorldScreen LOADED");
    }

    @Unique
    private String pomkots$lastPreset = "UNKNOWN";

    private static final String SEED_VALUE = "-7229487307260543510";
    private static final ResourceLocation CUSTOM_PRESET_LOC = new ResourceLocation("pomkotsmechs", "pomkots_world");
    private static final ResourceKey<WorldPreset> CUSTOM_PRESET_KEY = ResourceKey.create(Registries.WORLD_PRESET, CUSTOM_PRESET_LOC);
    private static final String SOURCE_PROFILE_NAME = "00_pomkots_world";
    private static final String CUSTOMIZED_PROFILE_NAME = "customized";

    /**
     * openFresh() 内の new CreateWorldScreen(...) 呼び出しの第4引数
     * (Optional<ResourceKey<WorldPreset>>) を差し替え、
     * LostCities導入時は pomkotsmechs:pomkots_world に変更する。
     */
    @ModifyArg(
        method = "openFresh",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/screens/worldselection/WorldCreationContext;Ljava/util/Optional;Ljava/util/OptionalLong;)V"
        ),
        index = 3
    )
    private static Optional<ResourceKey<WorldPreset>> modifyDefaultPreset(Optional<ResourceKey<WorldPreset>> original) {
        Optional<ResourceKey<WorldPreset>> result = original;
        if (ModList.get().isLoaded("lostcities") && PomkotsMechs.CONFIG.survivalModeEnabled) {
            result = Optional.of(CUSTOM_PRESET_KEY);
        }
        return result;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!ModList.get().isLoaded("lostcities") || !PomkotsMechs.CONFIG.survivalModeEnabled) {
            return;
        }

        CreateWorldScreen self = (CreateWorldScreen) (Object) this;
        WorldCreationUiState uiState = self.getUiState();

        uiState.addListener(state -> {
            String currentPreset = "";
            if (state.getWorldType() != null && state.getWorldType().preset() != null) {
                currentPreset = state.getWorldType().preset().unwrapKey()
                        .map(key -> key.location().toString())
                        .orElse("");
            }

            if (!currentPreset.equals(this.pomkots$lastPreset)) {
                this.pomkots$lastPreset = currentPreset;
                
                if (CUSTOM_PRESET_KEY.location().toString().equals(currentPreset)) {
                    // pomkots_world が選択された場合のみ、シード値と cities プロファイルを設定
                    if (!SEED_VALUE.equals(state.getSeed())) {
                        state.setSeed(SEED_VALUE);
                    }
                    var keepInv = state.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY);
                    if (keepInv != null && !keepInv.get()) {
                        keepInv.set(true, null);
                    }

                    LostCityProfile sourceProfile = ProfileSetup.STANDARD_PROFILES.get(SOURCE_PROFILE_NAME);
                    if (sourceProfile != null) {
                        LostCityProfile customizedProfile = new LostCityProfile(CUSTOMIZED_PROFILE_NAME, false);
                        ProfileSetup.STANDARD_PROFILES.put(CUSTOMIZED_PROFILE_NAME, customizedProfile);
                        customizedProfile.copyFrom(sourceProfile);
                        LostCitySetup.CLIENT_SETUP.setProfile(CUSTOMIZED_PROFILE_NAME);
                        mcjty.lostcities.gui.GuiLCConfig.selectProfile(CUSTOMIZED_PROFILE_NAME, customizedProfile);
                        mcjty.lostcities.worldgen.LostCityFeature.globalDimensionInfoDirtyCounter++;
                        Config.resetProfileCache();
                    } else {
                        Config.profileFromClient = SOURCE_PROFILE_NAME;
                    }
                } else {
                    if (SEED_VALUE.equals(state.getSeed())) {
                        state.setSeed("");
                    }
                    var keepInv = state.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY);
                    if (keepInv != null && keepInv.get()) {
                        keepInv.set(false, null);
                    }

                    mcjty.lostcities.gui.GuiLCConfig.selectProfile(null, null);
                    LostCitySetup.CLIENT_SETUP.setProfile(null);
                    Config.resetProfileCache();
                }
            }
        });

        var presetRegistry = uiState.getSettings().worldgenLoadContext().registryOrThrow(Registries.WORLD_PRESET);
        presetRegistry.getHolder(CUSTOM_PRESET_KEY).ifPresent(holder -> {
            uiState.setWorldType(new WorldCreationUiState.WorldTypeEntry(holder));
        });
    }
}