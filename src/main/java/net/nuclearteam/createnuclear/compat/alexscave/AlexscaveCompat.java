package net.nuclearteam.createnuclear.compat.alexscave;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.TremorzillaEggBlock;
import com.github.alexmodguy.alexscaves.server.entity.living.RaycatEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorzillaEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static com.github.alexmodguy.alexscaves.client.ClientProxy.random;

public class AlexscaveCompat {

    public AlexscaveCompat() {}

    // J'ai changé void en boolean
    public boolean MobSpawn(BlockState state, Level level, BlockPos.MutableBlockPos carve, float itemDropModifier, Explosion dummyExplosion){
        // Si c'est un oeuf de Tremorzilla
        if(state.is(ACBlockRegistry.TREMORZILLA_EGG.get()) && state.getBlock() instanceof TremorzillaEggBlock tremorzillaEggBlock){
            // On fait éclore le dinosaure
            tremorzillaEggBlock.spawnDinosaurs(level, carve, state);
            // On retourne TRUE pour dire "J'ai géré ce bloc, ne fais rien d'autre"
            return true;
        }
        // Logique normale pour les autres blocs Alex Caves
        else if (AlexsCaves.COMMON_CONFIG.nukesSpawnItemDrops.get() && random.nextFloat() < itemDropModifier && state.getFluidState().isEmpty()) {
            level.destroyBlock(carve, true);
        } else {
            state.onBlockExploded(level, carve, dummyExplosion);
        }
        return false;
    }

    public boolean isRaycat(LivingEntity entity) {
        return entity instanceof RaycatEntity;
    }

    public boolean isTremorzilla(LivingEntity entity){
        return entity instanceof TremorzillaEntity;
    }

    public boolean ACResConfig(){
        return AlexsCaves.COMMON_CONFIG.nukeMaxBlockExplosionResistance.get() <= 0;
    }

    public boolean ACDestroyable(BlockState state) {
        return !state.is(ACTagRegistry.NUKE_PROOF) &&
                (state.getBlock().getExplosionResistance() < AlexsCaves.COMMON_CONFIG.nukeMaxBlockExplosionResistance.get()
                        || state.is(ACBlockRegistry.TREMORZILLA_EGG.get()));
    }
}