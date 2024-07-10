package dev.createchronicles.core;

import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import java.lang.reflect.Field;

public class FluidModifier {
    public static void modifyShimmerFluid() {
        ForgeFlowingFluid shimmer = (ForgeFlowingFluid) ForgeRegistries.FLUIDS.getValue(new ResourceLocation("create_dd", "shimmer"));
        if (shimmer != null) {
            try {
                Field propertiesField = ForgeFlowingFluid.class.getDeclaredField("properties");
                propertiesField.setAccessible(true);
                ForgeFlowingFluid.Properties properties = (ForgeFlowingFluid.Properties) propertiesField.get(shimmer);

                Field canConvertToSourceField = ForgeFlowingFluid.Properties.class.getDeclaredField("canConvertToSource");
                canConvertToSourceField.setAccessible(true);
                canConvertToSourceField.setBoolean(properties, false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
