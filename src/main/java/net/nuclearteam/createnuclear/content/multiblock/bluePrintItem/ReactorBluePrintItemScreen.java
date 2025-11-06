package net.nuclearteam.createnuclear.content.multiblock.bluePrintItem;

import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CNPackets;
import net.nuclearteam.createnuclear.foundation.gui.CNGuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

@ParametersAreNonnullByDefault
@SuppressWarnings({"unused"})
public class ReactorBluePrintItemScreen extends AbstractSimiContainerScreen<ReactorBluePrintMenu> {
    protected static final CNGuiTextures BG = CNGuiTextures.CONFIGURED_PATTERN_GUI;

    public ReactorBluePrintItemScreen(ReactorBluePrintMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        setWindowSize(BG.width, BG.height + PLAYER_INVENTORY.getHeight());
        setWindowOffset(0, 0);
        super.init();
        clearWidgets();
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Dimensions "logiques" du GUI (texture + inventaire)
        int guiW = BG.width;
        int guiH = BG.height + PLAYER_INVENTORY.getHeight();

        // Dimensions réelles de l'écran (la Screen fournit width/height)
        int screenW = this.width;
        int screenH = this.height;

        // Calcul du scale pour faire tenir le GUI à l'écran (max 1.0)
        float scale = Math.min(1.0f, Math.min((float)screenW / (float)guiW, (float)screenH / (float)guiH));

        // Position à l'écran (en pixels) où l'on veut centrer le GUI après scale
        int scaledGuiW = (int)(guiW * scale);
        int scaledGuiH = (int)(guiH * scale);
        int targetX = (screenW - scaledGuiW) / 2;
        int targetY = (screenH - scaledGuiH) / 2;

        // On pousse la transformation : d'abord on translate au coin supérieur gauche voulu,
        // puis on scale. Après ce bloc, on dessine en coordonnées locales du GUI (0..guiW, 0..guiH).
        guiGraphics.pose().pushPose();
        // Translate en pixels *avant* le scale — on translate en coordonnées écran non-scalées,
        // donc on translate en targetX,targetY (puis on scale)
        guiGraphics.pose().translate(targetX, targetY, 0.0f);
        guiGraphics.pose().scale(scale, scale, 1.0f);

        // Ici on dessine en coordonnées "GUI logique". Si avant tu utilisais `x = leftPos; y = topPos+38;`
        // adapte : dessiner à (23, -19) etc comme dans ton code original (relatif à l'origine GUI).
        int originX = 23;     // équivalent à x+23 dans ton code
        int originY = -19;    // équivalent à y-19 dans ton code

        // Dessin du background dans les coordonnées locales
        BG.render(guiGraphics, originX, originY);

        // Inventaire joueur (position relative au GUI logique)
        renderPlayerInventory(guiGraphics, originX, originY + 194); // 175 dans ton code +19 décalage -> adapte si besoin

        // Titre (coordonnées locales aussi)
        guiGraphics.drawString(font, title, originX + 3, originY + 7, 0x592424, false); // ajuste les offsets si besoin

        guiGraphics.pose().popPose();

        // Si tu veux gérer le hover/clic, convertis mouseX/mouseY en coordonnées GUI locales si scale != 1.
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!ItemStack.matches(menu.player.getMainHandItem(), menu.contentHolder)) {
            menu.player.closeContainer();
        }

        float coef = 0.1F;

        CompoundTag tag = menu.contentHolder.getOrCreateTag();

        sendValueUpdate(tag, coef,
                tag.getInt("graphiteTime"),
                tag.getInt("uraniumTime"),
                tag.getInt("countUraniumRod"),
                tag.getInt("countGraphiteRod")+3
        );


    }


    private static void sendValueUpdate(CompoundTag tag, float heat, int graphiteTime, int uraniumTime, int countGraphiteRod, int countUraniumRod) {
        CNPackets.getChannel()
                .sendToServer(new ReactorBluePrintItemPacket(tag, heat, graphiteTime, uraniumTime, countGraphiteRod, countGraphiteRod));
    }
}