package net.nuclearteam.createnuclear.compat.alexscave;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.TremorzillaEggBlock;
import com.github.alexmodguy.alexscaves.server.entity.living.RaycatEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorzillaEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static com.github.alexmodguy.alexscaves.client.ClientProxy.random;

public class AlexscaveCompat {

    public AlexscaveCompat() {}

    public boolean MobSpawn(BlockState state, Level level, BlockPos.MutableBlockPos carve, float itemDropModifier, Explosion dummyExplosion){
        if(state.is(ACBlockRegistry.TREMORZILLA_EGG.get()) && state.getBlock() instanceof TremorzillaEggBlock tremorzillaEggBlock){
            tremorzillaEggBlock.spawnDinosaurs(level, carve, state);
            return true;
        }
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

    public void NukeParam() {
        ((ClientProxy) AlexsCaves.PROXY).renderNukeSkyDarkFor = 70;
        ((ClientProxy) AlexsCaves.PROXY).muteNonNukeSoundsFor = 50;
    }

    public SoundEvent[] GetACSounds(){
        return new SoundEvent[] {
                ACSoundRegistry.LARGE_NUCLEAR_EXPLOSION.get(),
                ACSoundRegistry.NUCLEAR_EXPLOSION.get(),
                ACSoundRegistry.NUCLEAR_EXPLOSION_RINGING.get(),
                ACSoundRegistry.NUCLEAR_EXPLOSION_RUMBLE.get()
        };
    }

    public boolean GetACConfig(){
        return AlexsCaves.CLIENT_CONFIG.nuclearBombFlash.get();
    }

    public void UpdateACProxy() {
        ((ClientProxy) AlexsCaves.PROXY).renderNukeFlashFor = 16;
    }
}