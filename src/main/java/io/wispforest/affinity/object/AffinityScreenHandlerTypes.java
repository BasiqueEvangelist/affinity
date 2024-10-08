package io.wispforest.affinity.object;

import io.wispforest.affinity.misc.screenhandler.AssemblyAugmentScreenHandler;
import io.wispforest.affinity.misc.screenhandler.OuijaBoardScreenHandler;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class AffinityScreenHandlerTypes implements AutoRegistryContainer<ScreenHandlerType<?>> {

    public static final ScreenHandlerType<RitualSocleComposerScreenHandler> RITUAL_SOCLE_COMPOSER = new ScreenHandlerType<>(RitualSocleComposerScreenHandler::client);
    public static final ScreenHandlerType<AssemblyAugmentScreenHandler> ASSEMBLY_AUGMENT = new ScreenHandlerType<>(AssemblyAugmentScreenHandler::client);
    public static final ScreenHandlerType<OuijaBoardScreenHandler> OUIJA_BOARD = new ScreenHandlerType<>(OuijaBoardScreenHandler::client);

    @Override
    public Registry<ScreenHandlerType<?>> getRegistry() {
        return Registries.SCREEN_HANDLER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ScreenHandlerType<?>> getTargetFieldType() {
        return (Class<ScreenHandlerType<?>>) (Object) ScreenHandlerType.class;
    }
}
