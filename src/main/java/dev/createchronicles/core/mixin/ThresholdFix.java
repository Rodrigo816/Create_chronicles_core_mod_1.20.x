package dev.createchronicles.core.mixin;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchObservable;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlock;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.compat.storageDrawers.StorageDrawers;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.lang.reflect.Field;

@Mixin(value = ThresholdSwitchBlockEntity.class, remap = false)
public abstract class ThresholdFix extends SmartBlockEntity {

    // Constructor to match the superclass
    public ThresholdFix(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    /**
     * @author simibubi
     * @reason Fix not working with sophisticatedStorage
     */
    @Overwrite
    public void updateCurrentLevel() {
        boolean changed = false;
        float occupied = 0;
        float totalSpace = 0;
        float prevLevel = getCurrentLevel();

        InvManipulationBehaviour observedInventory = getObservedInventory();
        TankManipulationBehaviour observedTank = getObservedTank();
        VersionedInventoryTrackerBehaviour invVersionTracker = getInvVersionTracker();
        FilteringBehaviour filtering = getFilteringBehaviour();

        observedInventory.findNewCapability();
        observedTank.findNewCapability();

        BlockPos target = worldPosition.relative(ThresholdSwitchBlock.getTargetDirection(getBlockState()));
        BlockEntity targetBlockEntity = level.getBlockEntity(target);

        if (targetBlockEntity instanceof ThresholdSwitchObservable observable) {
            setCurrentLevel(observable.getPercent() / 100f);
        } else if (StorageDrawers.isDrawer(targetBlockEntity) && observedInventory.hasInventory()) {
            setCurrentLevel(StorageDrawers.getTrueFillLevel(observedInventory.getInventory(), filtering));
        } else if (observedInventory.hasInventory() || observedTank.hasInventory()) {
            if (observedInventory.hasInventory()) {
                // Item inventory
                IItemHandler inv = observedInventory.getInventory();
                if (invVersionTracker.stillWaiting(inv)) {
                    occupied = prevLevel;
                    totalSpace = 1f;
                } else {
                    invVersionTracker.awaitNewVersion(inv);
                    final int VANILLA_SLOT_LIMIT = 64;
                    for (int slot = 0; slot < inv.getSlots(); slot++) {
                        ItemStack stackInSlot = inv.getStackInSlot(slot);
                        int space = inv.getSlotLimit(slot) / (VANILLA_SLOT_LIMIT / stackInSlot.getMaxStackSize());
                        int count = stackInSlot.getCount();
                        if (space == 0) continue;

                        totalSpace += 1;
                        if (filtering.test(stackInSlot))
                            occupied += count * (1f / space);
                    }
                }
            }

            if (observedTank.hasInventory()) {
                // Fluid inventory
                IFluidHandler tank = observedTank.getInventory();
                for (int slot = 0; slot < tank.getTanks(); slot++) {
                    FluidStack stackInSlot = tank.getFluidInTank(slot);
                    int space = tank.getTankCapacity(slot);
                    int count = stackInSlot.getAmount();
                    if (space == 0) continue;

                    totalSpace += 1;
                    if (filtering.test(stackInSlot))
                        occupied += count * (1f / space);
                }
            }

            setCurrentLevel(occupied / totalSpace);

        } else {
            // No compatible inventories found
            if (getCurrentLevel() == -1) return;
            level.setBlock(worldPosition, getBlockState().setValue(ThresholdSwitchBlock.LEVEL, 0), 3);
            setCurrentLevel(-1);
            setRedstoneState(false);
            sendData();
            invokeScheduleBlockTick();
            return;
        }

        setCurrentLevel(Mth.clamp(getCurrentLevel(), 0, 1));
        changed = getCurrentLevel() != prevLevel;

        boolean previouslyPowered = getRedstoneState();
        if (getRedstoneState() && getCurrentLevel() <= getOffWhenBelow())
            setRedstoneState(false);
        else if (!getRedstoneState() && getCurrentLevel() >= getOnWhenAbove())
            setRedstoneState(true);
        boolean update = previouslyPowered != getRedstoneState();

        int displayLevel = 0;
        if (getCurrentLevel() > 0)
            displayLevel = (int) (1 + getCurrentLevel() * 4);
        level.setBlock(worldPosition, getBlockState().setValue(ThresholdSwitchBlock.LEVEL, displayLevel), update ? 3 : 2);

        if (update)
            invokeScheduleBlockTick();

        if (changed || update) {
            DisplayLinkBlock.notifyGatherers(level, worldPosition);
            notifyUpdate();
        }
    }

    private float getCurrentLevel() {
        try {
            Field currentLevelField = ThresholdSwitchBlockEntity.class.getDeclaredField("currentLevel");
            currentLevelField.setAccessible(true);
            return currentLevelField.getFloat(this);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void setCurrentLevel(float value) {
        try {
            Field currentLevelField = ThresholdSwitchBlockEntity.class.getDeclaredField("currentLevel");
            currentLevelField.setAccessible(true);
            currentLevelField.setFloat(this, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InvManipulationBehaviour getObservedInventory() {
        try {
            Field field = ThresholdSwitchBlockEntity.class.getDeclaredField("observedInventory");
            field.setAccessible(true);
            return (InvManipulationBehaviour) field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private TankManipulationBehaviour getObservedTank() {
        try {
            Field field = ThresholdSwitchBlockEntity.class.getDeclaredField("observedTank");
            field.setAccessible(true);
            return (TankManipulationBehaviour) field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private VersionedInventoryTrackerBehaviour getInvVersionTracker() {
        try {
            Field field = ThresholdSwitchBlockEntity.class.getDeclaredField("invVersionTracker");
            field.setAccessible(true);
            return (VersionedInventoryTrackerBehaviour) field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private FilteringBehaviour getFilteringBehaviour() {
        try {
            Field field = ThresholdSwitchBlockEntity.class.getDeclaredField("filtering");
            field.setAccessible(true);
            return (FilteringBehaviour) field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean getRedstoneState() {
        try {
            Field field = ThresholdSwitchBlockEntity.class.getDeclaredField("redstoneState");
            field.setAccessible(true);
            return field.getBoolean(this);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setRedstoneState(boolean value) {
        try {
            Field field = ThresholdSwitchBlockEntity.class.getDeclaredField("redstoneState");
            field.setAccessible(true);
            field.setBoolean(this, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float getOffWhenBelow() {
        try {
            Field field = ThresholdSwitchBlockEntity.class.getDeclaredField("offWhenBelow");
            field.setAccessible(true);
            return field.getFloat(this);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private float getOnWhenAbove() {
        try {
            Field field = ThresholdSwitchBlockEntity.class.getDeclaredField("onWhenAbove");
            field.setAccessible(true);
            return field.getFloat(this);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void invokeScheduleBlockTick() {
        try {
            ThresholdSwitchBlockEntity.class.getDeclaredMethod("scheduleBlockTick").invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}