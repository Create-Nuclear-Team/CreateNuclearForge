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
import net.nuclearteam.createnuclear.CreateNuclear;

import static com.github.alexmodguy.alexscaves.client.ClientProxy.random;

public class AlexscaveCompat {

    public AlexscaveCompat() {}

    public void MobSpawn(BlockState state, Level level, BlockPos.MutableBlockPos carve, float itemDropModifier, Explosion dummyExplosion){
        CreateNuclear.LOGGER.warn("Entity explosion compatibility with Alex's Cave - In the function");
        if(state.is(ACBlockRegistry.TREMORZILLA_EGG.get()) && state.getBlock() instanceof TremorzillaEggBlock tremorzillaEggBlock){
            tremorzillaEggBlock.spawnDinosaurs(level, carve, state);
        }else if (AlexsCaves.COMMON_CONFIG.nukesSpawnItemDrops.get() && random.nextFloat() < itemDropModifier && state.getFluidState().isEmpty()) {
            level.destroyBlock(carve, true);
        } else {
            state.onBlockExploded(level, carve, dummyExplosion);
        }
    }

    public void RaycatImmunity(LivingEntity entity, float damage){
        if (entity instanceof RaycatEntity) {
            damage = 0;
        }
    }

    public void TremorzillaImmunity(LivingEntity entity, float damage, float playerFling){
        if(entity instanceof TremorzillaEntity){
            playerFling = 0;
            damage = 0;
        }
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
