package net.nuclearteam.createnuclear.content.explosion;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CNTextureOffset {
    public final int textureOffsetX;
    public final int textureOffsetY;

    public CNTextureOffset(int textureOffsetXIn, int textureOffsetYIn) {
        this.textureOffsetX = textureOffsetXIn;
        this.textureOffsetY = textureOffsetYIn;
    }
}