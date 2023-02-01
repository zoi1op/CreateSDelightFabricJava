package phoupraw.mcmod.createsdelight.registry;

import net.minecraft.block.MapColor;
import net.minecraft.util.Identifier;
import phoupraw.mcmod.createsdelight.CreateSDelight;
import phoupraw.mcmod.createsdelight.api.VirtualFluid;
public final class MyFluids {
    public static final VirtualFluid SUNFLOWER_OIL = new VirtualFluid.Builder().withId(new Identifier(CreateSDelight.MOD_ID,"sunflower_oil")).withItemGroup(MyItems.ITEM_GROUP).withTint(MapColor.TERRACOTTA_YELLOW.color).buildAndRegister();

    private MyFluids() {}
}