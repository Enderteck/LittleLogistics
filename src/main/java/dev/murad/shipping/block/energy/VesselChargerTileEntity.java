package dev.murad.shipping.block.energy;

import dev.murad.shipping.ShippingConfig;
import dev.murad.shipping.block.IVesselLoader;
import dev.murad.shipping.capability.ReadWriteEnergyStorage;
import dev.murad.shipping.entity.custom.VesselEntity;
import dev.murad.shipping.setup.ModTileEntitiesTypes;
import net.minecraft.block.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.Player;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VesselChargerTileEntity extends BlockEntity implements TickableBlockEntity, IVesselLoader {
    private static final int MAX_TRANSFER = ShippingConfig.Server.VESSEL_CHARGER_BASE_MAX_TRANSFER.get();
    private static final int MAX_CAPACITY = ShippingConfig.Server.VESSEL_CHARGER_BASE_CAPACITY.get();
    private final ReadWriteEnergyStorage internalBattery = new ReadWriteEnergyStorage(MAX_CAPACITY, MAX_TRANSFER, MAX_TRANSFER);
    private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> internalBattery);
    private int cooldownTime = 0;

    public VesselChargerTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntitiesTypes.VESSEL_CHARGER.get(), pos, state);
        internalBattery.setEnergy(0);
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityEnergy.ENERGY)
            return holder.cast();
        return super.getCapability(capability, facing);
    }


    @Override
    public void tick() {
        if (this.level != null && !this.level.isClientSide) {
            --this.cooldownTime;
            if (this.cooldownTime <= 0) {
                this.cooldownTime = tryChargeEntity() ? 0 : 10;
            }
        }
    }


    private boolean tryChargeEntity() {
        return IVesselLoader.getEntityCapability(getBlockPos().relative(getBlockState().getValue(VesselChargerBlock.FACING)),
                CapabilityEnergy.ENERGY, level).map(iEnergyStorage -> {
                    int vesselCap = iEnergyStorage.receiveEnergy(MAX_TRANSFER, true);
                    int toTransfer = internalBattery.extractEnergy(vesselCap, false);
                    return iEnergyStorage.receiveEnergy(toTransfer, false) > 0;
        }).orElse(false);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        internalBattery.readAdditionalSaveData(compound.getCompound("energy_storage"));
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        CompoundNBT energyNBT = new CompoundNBT();
        internalBattery.addAdditionalSaveData(energyNBT);
        CompoundNBT sup = super.save(compound);
        sup.put("energy_storage", energyNBT);
        return sup;
    }

    @Override
    public boolean holdVessel(VesselEntity vessel, Mode mode) {
        return vessel.getCapability(CapabilityEnergy.ENERGY).map(energyHandler -> {
            switch (mode) {
                case EXPORT:
                    return (energyHandler.getEnergyStored() < energyHandler.getMaxEnergyStored() - 50) && internalBattery.getEnergyStored() > 50;
                default:
                    return false;
            }
        }).orElse(false);
    }

    public void use(Player player, Hand hand) {
        player.displayClientMessage(new TranslationTextComponent("block.littlelogistics.vessel_charger.capacity",
                internalBattery.getEnergyStored(), internalBattery.getMaxEnergyStored()), false);
    }
}
