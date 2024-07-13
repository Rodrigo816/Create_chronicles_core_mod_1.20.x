package dev.createchronicles.core;
import com.mojang.logging.LogUtils;
import com.yungnickyoung.minecraft.ribbits.entity.trade.ItemListing;
import com.yungnickyoung.minecraft.ribbits.entity.trade.ItemsForAmethysts;
import com.yungnickyoung.minecraft.ribbits.module.RibbitProfessionModule;
import com.yungnickyoung.minecraft.ribbits.module.RibbitTradeModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(CreateChroniclesCore.MOD_ID)
public class CreateChroniclesCore {
    public static final String MOD_ID = "create_chronicles_core";
    public CreateChroniclesCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        RibbitTradeModule.TRADES.put(RibbitProfessionModule.MERCHANT, new ItemListing[]{
                new ItemsForAmethysts(getQuarkItem("quark:indigo_corundum_cluster"), 64, 64, 2, 2, 16),
                new ItemsForAmethysts(getQuarkItem("quark:violet_corundum_cluster"), 64, 64, 2, 2, 16),
                new ItemsForAmethysts(getQuarkItem("quark:white_corundum_cluster"), 64, 64, 2, 2, 16),
                new ItemsForAmethysts(getQuarkItem("quark:black_corundum_cluster"), 64, 64, 2, 2, 16),
                new ItemsForAmethysts(getQuarkItem("quark:yellow_corundum_cluster"), 64, 64, 2, 2, 16),
        });
        RibbitTradeModule.TRADES.put(RibbitProfessionModule.FISHERMAN, new ItemListing[]{
                new ItemsForAmethysts(getQuarkItem("quark:red_corundum_cluster"), 64, 64, 2, 2, 16),
                new ItemsForAmethysts(getQuarkItem("quark:orange_corundum_cluster"), 64, 64, 2, 2, 16),
                new ItemsForAmethysts(getQuarkItem("quark:yellow_corundum_cluster"), 64, 64, 2, 2, 16),
                new ItemsForAmethysts(getQuarkItem("quark:green_corundum_cluster"), 64, 64, 2, 2, 16),
                new ItemsForAmethysts(getQuarkItem("quark:blue_corundum_cluster"), 64, 64, 2, 2, 16),
        });
    }

    private static Item getQuarkItem(String registryName) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
    }
}