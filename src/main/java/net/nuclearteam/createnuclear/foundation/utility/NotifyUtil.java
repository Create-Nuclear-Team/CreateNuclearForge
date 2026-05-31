package net.nuclearteam.createnuclear.foundation.utility;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class NotifyUtil {


    public static void sendActionBar(Level level, BlockPos pos, LangBuilder message, ChatFormatting color, int radius, boolean warnAll) {
        sendActionBar(level, pos, message.component(), color, radius, warnAll);
    }

    /**
     * Envoie un message dans l'Action Bar (au-dessus de la barre d'inventaire).
     * Plus propre et moins intrusif que les titres géants.
     * * @param level    Le monde (côté serveur)
     * @param pos      Position centrale pour le rayon d'action
     * @param message  Le texte à afficher
     * @param color    La couleur du texte
     * @param radius   Distance maximale pour recevoir l'alerte
     * @param warnAll  Si vrai, tout le serveur reçoit le message
     */
    public static void sendActionBar(Level level, BlockPos pos, MutableComponent message, ChatFormatting color, int radius, boolean warnAll) {
        if (level.isClientSide) return;

        // Création du composant texte en GRAS avec la couleur choisie
        Component actionBarComp = message.withStyle(color, ChatFormatting.BOLD);

        List<ServerPlayer> targets = getTargetPlayers(level, pos, radius, warnAll);

        for (ServerPlayer player : targets) {
            // Le paramètre 'true' indique que le message va dans l'Action Bar
            player.displayClientMessage(actionBarComp, true);
        }
    }

    /**
     * Envoie un titre et un sous-titre géant au milieu de l'écran.
     */
    public static void sendTitle(Level level, BlockPos pos, String title, String subtitle, ChatFormatting color,
                                 int radius, boolean warnAll, int fadeIn, int stay, int fadeOut) {

        if (level.isClientSide) return;

        Component titleComp = Component.literal(title).withStyle(color, ChatFormatting.BOLD);
        Component subtitleComp = Component.literal(subtitle).withStyle(ChatFormatting.WHITE);

        List<ServerPlayer> targets = getTargetPlayers(level, pos, radius, warnAll);

        for (ServerPlayer serverPlayer : targets) {
            serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
            serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(subtitleComp));
            serverPlayer.connection.send(new ClientboundSetTitleTextPacket(titleComp));
        }
    }

    /**
     * Raccourci pour une alerte de titre rapide.
     */
    public static void quickAlert(Level level, BlockPos pos, String title, String sub, ChatFormatting color, int radius, boolean warnAll) {
        sendTitle(level, pos, title, sub, color, radius, warnAll, 10, 50, 10);
    }

    /**
     * Utilitaire interne pour récupérer la liste des joueurs à notifier selon les réglages.
     */
    private static List<ServerPlayer> getTargetPlayers(Level level, BlockPos pos, int radius, boolean warnAll) {
        List<ServerPlayer> players = new ArrayList<>();

        if (level.getServer() == null) return players;

        if (warnAll) {
            players.addAll(level.getServer().getPlayerList().getPlayers());
        } else {
            AABB area = new AABB(pos).inflate(radius);
            for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                if (area.contains(p.getX(), p.getY(), p.getZ())) {
                    players.add(p);
                }
            }
        }
        return players;
    }
}