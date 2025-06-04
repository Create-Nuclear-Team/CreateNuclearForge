package net.nuclearteam.createnuclear.content.decoration.palettes;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.nuclearteam.createnuclear.CNCreativeModeTabs;
import net.nuclearteam.createnuclear.CreateNuclear;

public class CNPaletteBlocks {
    private static final CreateRegistrate REGISTRATE = CreateNuclear.REGISTRATE;

    static {
        REGISTRATE.setCreativeTab(CNCreativeModeTabs.MAIN);
        CNPaletteStoneTypes.register(REGISTRATE);
    }

    public static void register() {}


}
