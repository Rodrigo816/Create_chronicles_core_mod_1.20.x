package dev.createchronicles.core.mixin;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeltDeployerCallbacks.class)
public abstract class MixinBeltDeployerCallbacks {

    @Inject(method = "activate(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour;Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;)V", at = @At("HEAD"), remap = false)
    private static void makeItemUnbreakableIfModifierPresent(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler, DeployerBlockEntity blockEntity, Recipe<?> recipe, CallbackInfo ci) {
        ItemStack heldItem = blockEntity.getPlayer().getMainHandItem();
        CompoundTag tag = heldItem.getTag();

        if (tag != null && tag.contains("Modifier") && "forbidden_arcanus:eternal".equals(tag.getString("Modifier"))) {
            tag.putBoolean("Unbreakable", true);
            heldItem.setTag(tag);
        }
    }
}