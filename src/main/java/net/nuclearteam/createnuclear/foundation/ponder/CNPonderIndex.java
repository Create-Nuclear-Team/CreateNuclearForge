package net.nuclearteam.createnuclear.foundation.ponder;

import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.infrastructure.ponder.DebugScenes;

import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CreateNuclear;

import static com.simibubi.create.infrastructure.ponder.AllPonderTags.KINETIC_SOURCES;

public class CNPonderIndex {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(CreateNuclear.MOD_ID);
    public static final boolean REGISTER_DEBUG_SCENES = false;

    public static void register() {
        // Reactor
        HELPER.forComponents(CNBlocks.REACTOR_CONTROLLER)
                .addStoryBoard("reactor/setup", CNPonderReactor::init)
                .addStoryBoard("reactor/setup", CNPonderReactor::enable);


        HELPER.forComponents(CNItems.REACTOR_BLUEPRINT)
                .addStoryBoard("reactor/setup", CNPonderReactor::enable);

        PonderRegistry.TAGS.forTag(KINETIC_SOURCES)
                .add(CNBlocks.REACTOR_CONTROLLER)
        ;

        if (REGISTER_DEBUG_SCENES)
            DebugScenes.registerAll();
    }
}