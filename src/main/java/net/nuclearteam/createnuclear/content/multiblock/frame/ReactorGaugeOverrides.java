package net.nuclearteam.createnuclear.content.multiblock.frame;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;

public class ReactorGaugeOverrides {
    public static ItemModelBuilder addOverrideModels(DataGenContext<Item, ReactorframeItem> c, RegistrateItemModelProvider p) {
        ItemModelBuilder builder = p.generated(c);
        /*CreateNuclear.LOGGER.warn(" " + builder.override()
                .model(p.getBuilder("block/reactor_main_frame/reactor_gauge_none")
                        .parent(p.getExistingFile(p.modLoc("block/reactor_main_frame/reactor_gauge_none"))))
                .end().toJson());*/
        return builder.override()
                .model(p.getBuilder("block/reactor_main_frame/reactor_gauge_none")
                        .parent(p.getExistingFile(p.modLoc("block/reactor_main_frame/reactor_gauge_none"))))
                .end();
        //.predicate(CreateNuclear.asResource("reactor_main_frame"), 0)
    }
}
