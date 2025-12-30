package net.nuclearteam.createnuclear;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.nuclearteam.createnuclear.content.redstone.displayLink.source.PatternCountRodsDisplaySource;

import java.util.function.Supplier;

public class CNDisplaySources {
    private static final CreateRegistrate REGISTRATE = CreateNuclear.REGISTRATE;

    public static final RegistryEntry<PatternCountRodsDisplaySource> PATTERN_COUNT_RODS = simple("pattern_count_rods", PatternCountRodsDisplaySource::new);

    private static <T extends DisplaySource> RegistryEntry<T> simple(String name, Supplier<T> supplier) {
        return REGISTRATE.displaySource(name, supplier).register();
    }

    public static void register() {
    }
}
