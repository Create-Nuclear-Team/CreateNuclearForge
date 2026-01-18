package net.nuclearteam.createnuclear.foundation.utility;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class NotifyUtil {

    /**
     * Envoie un titre et un sous-titre à un groupe de joueurs défini par les paramètres.
     * @param level         Le monde (Serveur)
     * @param pos           Position d'origine (ignorée si warnAll est vrai)
     * @param title         Texte principal
     * @param subtitle      Texte secondaire
     * @param color         Couleur du titre principal
     * @param radius        Rayon d'action (si warnAll est faux)
     * @param warnAll       Si vrai, ignore le radius et prévient tout le serveur
     * @param fadeIn        Temps d'apparition (ticks)
     * @param stay          Temps d'affichage (ticks)
     * @param fadeOut       Temps de disparition (ticks)
     */
    public static void sendTitle(Level level, BlockPos pos, String title, String subtitle, ChatFormatting color,
                                 int radius, boolean warnAll, int fadeIn, int stay, int fadeOut) {

        if (level.isClientSide) return;

        Component titleComp = Component.literal(title).withStyle(color, ChatFormatting.BOLD);
        Component subtitleComp = Component.literal(subtitle).withStyle(ChatFormatting.WHITE);

        List<ServerPlayer> targets;

        if (warnAll && level.getServer() != null) {
            // Cible : Tout le monde sur le serveur
            targets = (List<ServerPlayer>) (Object) level.getServer().getPlayerList().getPlayers();
        } else {
            // Cible : Joueurs dans le rayon autour de la position
            AABB area = new AABB(pos).inflate(radius);
            targets = (List<ServerPlayer>) (Object) level.getEntitiesOfClass(Player.class, area);
        }

        for (ServerPlayer serverPlayer : targets) {
            serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
            serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(subtitleComp));
            serverPlayer.connection.send(new ClientboundSetTitleTextPacket(titleComp));
        }
    }

    /**
     * Raccourci pour une alerte rapide avec des temps par défaut (2.5 secondes d'affichage)
     */
    public static void quickAlert(Level level, BlockPos pos, String title, String sub, ChatFormatting color, int radius, boolean warnAll) {
        sendTitle(level, pos, title, sub, color, radius, warnAll, 10, 50, 10);
    }
}