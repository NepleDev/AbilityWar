package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.config.AbilityWarSettings.Settings.DeathSettings;
import daybreak.abilitywar.game.events.ParticipantDeathEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.language.KoreanUtil;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Death Manager
 * @author Daybreak 새벽
 */
public class DeathManager implements Listener {

    private final Game game;

    public DeathManager(Game game) {
        this.game = game;
        game.registerListener(this);
    }

    @EventHandler
    public final void onPlayerDeath(PlayerDeathEvent e) {
        Player victimPlayer = e.getEntity();
        Player killerPlayer = victimPlayer.getKiller();
        if (victimPlayer.getLastDamageCause() != null) {
            if (killerPlayer != null) {
                e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&a" + killerPlayer.getName() + "&f님이 &c" + victimPlayer.getName() + "&f님을 죽였습니다."));
            } else {
                switch (victimPlayer.getLastDamageCause().getCause()) {
                    case CONTACT:
                        e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 찔려 죽었습니다."));
                        break;
                    case FALL:
                        e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어져 죽었습니다."));
                        break;
                    case FALLING_BLOCK:
                        e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어지는 블록에 맞아 죽었습니다."));
                        break;
                    case SUFFOCATION:
                        e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 끼여 죽었습니다."));
                        break;
                    case DROWNING:
                        e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 익사했습니다."));
                        break;
                    case ENTITY_EXPLOSION:
                        e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 폭발했습니다."));
                        break;
                    case LAVA:
                        e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 용암에 빠져 죽었습니다."));
                        break;
                    case FIRE:
                    case FIRE_TICK:
                        e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 노릇노릇하게 구워졌습니다."));
                        break;
                    default:
                        e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
                        break;
                }
            }
        } else {
            e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
        }

        if (game.isParticipating(victimPlayer)) {
            Participant victim = game.getParticipant(victimPlayer);

            if (DeathSettings.getItemDrop()) {
                e.setKeepInventory(false);
                victimPlayer.getInventory().clear();
            } else {
                e.setKeepInventory(true);
            }

            Bukkit.getPluginManager().callEvent(new ParticipantDeathEvent(victim));

            if (DeathSettings.getAbilityReveal()) {
                if (victim.hasAbility()) {
                    String name = victim.getAbility().getName();
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                            "&f[&c능력&f] &c" + victimPlayer.getName() + "&f님의 능력은 "
                                    + KoreanUtil.getCompleteWord("&e" + name, "&f이었", "&f였") + "습니다."));
                } else {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                            "&f[&c능력&f] &c" + victimPlayer.getName() + "&f님은 능력이 없습니다."));
                }
            }
            if (DeathSettings.getAbilityRemoval()) {
                victim.removeAbility();
            }

            Operation(victim);
        }
    }

    protected void Operation(Participant victim) {
        switch (DeathSettings.getOperation()) {
            case 탈락:
                Eliminate(victim);
                break;
            case 관전모드:
                victim.getPlayer().setGameMode(GameMode.SPECTATOR);
                break;
            case 없음:
                break;
        }
    }

    /**
     * 탈락된 유저 UUID 목록
     */
    private final ArrayList<UUID> eliminated = new ArrayList<>();

    /**
     * Operation 콘피그에 따라 탈락, 관전모드 설정 또는 아무 행동도 하지 않을 수 있습니다.
     *
     * @param participant 작업을 처리할 참가자
     */
    public final void Eliminate(Participant participant) {
        Player player = participant.getPlayer();
        eliminated.add(player.getUniqueId());
        player.kickPlayer(Messager.defaultPrefix + "\n" + ChatColor.translateAlternateColorCodes('&', "&f탈락하셨습니다."));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c" + player.getName() + "&f님이 탈락하셨습니다."));
    }

    /**
     * 플레이어의 탈락 여부를 확인합니다.
     */
    public final boolean isEliminated(Player p) {
        return eliminated.contains(p.getUniqueId());
    }

    public interface Handler {
        DeathManager getDeathManager();
    }

}