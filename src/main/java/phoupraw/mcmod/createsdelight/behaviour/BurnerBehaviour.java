package phoupraw.mcmod.createsdelight.behaviour;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import phoupraw.mcmod.createsdelight.registry.MyFluids;
public class BurnerBehaviour extends TileEntityBehaviour {
    public static final BehaviourType<BurnerBehaviour> TYPE = new BehaviourType<>("burner");
    public @Nullable Storage<ItemVariant> itemS;
    public @Nullable Storage<FluidVariant> fluidS;
    private int fuelTicks;

    public BurnerBehaviour(SmartTileEntity te) {
        super(te);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (getWorld().isClient()) return;
        if (itemS == null) {
            itemS = ItemStorage.SIDED.find(getWorld(), getPos(), tileEntity.getCachedState(), tileEntity, null);
        }
        if (fluidS == null) {
            fluidS = FluidStorage.SIDED.find(getWorld(), getPos(), tileEntity.getCachedState(), tileEntity, null);
        }
    }

    @Override
    public void write(NbtCompound nbt, boolean clientPacket) {
        super.write(nbt, clientPacket);
        nbt.putInt("fuelTicks", getFuelTicks());
    }

    @Override
    public void read(NbtCompound nbt, boolean clientPacket) {
        super.read(nbt, clientPacket);
        setFuelTicks(nbt.getInt("fuelTicks"));
    }

    @Override
    public void tick() {
        super.tick();
        if (getFuelTicks() <= 0) {return;}
        setFuelTicks(getFuelTicks() - 1);
        if (getFuelTicks() != 0) {return;}
        tryIgnite();
    }

    public int tryIgnite() {
        if (itemS != null) {
            for (var view : itemS) {
                var resource = view.getResource();
                Integer fuelTime = FuelRegistry.INSTANCE.get(resource.getItem());
                if (fuelTime == null) continue;
                try (var transa = Transaction.openOuter()) {
                    long amount = view.extract(resource, 1, transa);
                    if (amount == 1) {
                        transa.commit();
                        setFuelTicks(getFuelTicks() + fuelTime);
                        return fuelTime;
                    }
                }
            }
        }
        if (fluidS != null) {
            for (var view : fluidS) {
                var resource = view.getResource();
                if (!resource.isOf(MyFluids.SUNFLOWER_OIL)) continue;
                try (var transa = Transaction.openOuter()) {
                    long amount = view.extract(resource, FluidConstants.NUGGET, transa);
                    if (amount > 0) {
                        transa.commit();
                        int fuelTime = (int) (20 * amount / FluidConstants.NUGGET);
                        setFuelTicks(getFuelTicks() + fuelTime);
                        return fuelTime;
                    }
                }
            }
        }
        return 0;
    }
    public int getFuelTicks() {
        return fuelTicks;
    }

    public void setFuelTicks(int fuelTicks) {
        int p = getFuelTicks();
        this.fuelTicks = fuelTicks;
        if (p <= 0 && fuelTicks > 0) {
            onIgnite();
        } else if (p > 0 && fuelTicks <= 0) {
            onExtinguish();
        }
    }

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    public void onIgnite() {
        tileEntity.sendData();
    }

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    public void onExtinguish() {
        tileEntity.sendData();
    }
}
