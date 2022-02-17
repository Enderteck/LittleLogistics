package dev.murad.shipping.entity.container;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.murad.shipping.ShippingMod;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.TranslationTextComponent;

// Todo: consolidate tug screen code
public class EnergyTugScreen extends AbstractTugScreen<EnergyTugContainer> {
    private static final ResourceLocation GUI = new ResourceLocation(ShippingMod.MOD_ID, "textures/container/energy_tug.png");

    public EnergyTugScreen(EnergyTugContainer menu, Inventory playerInventory, Component label) {
        super(menu, playerInventory, label);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (inBounds(mouseX - leftPos, mouseY - topPos, 56, 17, 68, 67)) {
            this.renderTooltip(matrixStack,
                    new TranslationTextComponent("screen.littlelogistics.energy_tug.energy",
                            getMenu().getEnergy(),
                            getMenu().getCapacity()),
                    mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        this.minecraft.getTextureManager().bind(GUI);
        int i = this.getGuiLeft();
        int j = this.getGuiTop();
        this.blit(matrixStack, i, j, 0, 0, this.getXSize(), this.getYSize());

        double r = this.menu.getEnergyCapacityRatio();
        int k = (int) (r * 50);
        this.blit(matrixStack, i + 56, j + 17 + 50 - k, 176, 50 - k, 12, k);
    }
}
