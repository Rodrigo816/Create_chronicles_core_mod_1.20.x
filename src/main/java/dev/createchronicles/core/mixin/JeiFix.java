package dev.createchronicles.core.mixin;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({CreateRegistrate.class})
public class JeiFix {
    @Shadow(remap = false)
    @Final
    private static Map<RegistryEntry<?>, RegistryObject<CreativeModeTab>> TAB_LOOKUP = Collections.synchronizedMap(new IdentityHashMap());
}