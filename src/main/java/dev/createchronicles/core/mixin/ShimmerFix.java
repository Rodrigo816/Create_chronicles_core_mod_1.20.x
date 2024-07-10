package dev.createchronicles.core.mixin;

import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ForgeFlowingFluid.Properties.class)
public class ShimmerFix {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        ForgeFlowingFluid.Properties properties = (ForgeFlowingFluid.Properties) (Object) this;
        // Assuming you have some way to identify your fluid properties
        try {
            Field canConvertToSourceField = ForgeFlowingFluid.Properties.class.getDeclaredField("canConvertToSource");
            canConvertToSourceField.setAccessible(true);
            canConvertToSourceField.setBoolean(properties, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
