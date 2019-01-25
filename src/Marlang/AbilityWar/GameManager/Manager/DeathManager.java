package Marlang.AbilityWar.GameManager.Manager;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent.Progress;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Game;
import Marlang.AbilityWar.Utils.Messager;

/**
 * Death Manager
 * @author _Marlang ����
 */
public class DeathManager implements Listener {
	
	private Game game;
	
	public DeathManager(Game game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}
	
	/**
	 * ���� ����� Listener Unregister
	 */
	@EventHandler
	public void onGameProcess(AbilityWarProgressEvent e) {
		if(e.getProgress().equals(Progress.Game_ENDED)) {
			HandlerList.unregisterAll(this);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent e) {
		Player Victim = e.getEntity();
		Player Killer = Victim.getKiller();
		if(Victim.getLastDamageCause() != null) {
			DamageCause Cause = Victim.getLastDamageCause().getCause();

			if(Killer != null) {
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&a" + Killer.getName() + "&f���� &c" + Victim.getName() + "&f���� �׿����ϴ�."));
			} else {
				if(Cause.equals(DamageCause.CONTACT)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� ��� �׾����ϴ�."));
				} else if(Cause.equals(DamageCause.FALL)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� ������ �׾����ϴ�."));
				} else if(Cause.equals(DamageCause.FALLING_BLOCK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� �������� ���Ͽ� �¾� �׾����ϴ�."));
				} else if(Cause.equals(DamageCause.SUFFOCATION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� ���� �׾����ϴ�."));
				} else if(Cause.equals(DamageCause.DROWNING)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� ���� ���� �׾����ϴ�."));
				} else if(Cause.equals(DamageCause.ENTITY_EXPLOSION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� �����Ͽ����ϴ�."));
				} else if(Cause.equals(DamageCause.LAVA)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� ��Ͽ� ���� �׾����ϴ�."));
				} else if(Cause.equals(DamageCause.FIRE) || Cause.equals(DamageCause.FIRE_TICK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� �븩�븩�ϰ� ���������ϴ�."));
				} else {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� �׾����ϴ�."));
				}
			}
		} else {
			e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f���� �׾����ϴ�."));
		}

		if(game.isGameStarted()) {
			if(AbilityWarSettings.getItemDrop()) {
				e.setKeepInventory(false);
			} else {
				e.setKeepInventory(true);
			}
			if(game.getPlayers().contains(Victim)) {
				if(AbilityWarSettings.getAbilityReveal()) {
					if(game.getAbilities().containsKey(Victim)) {
						AbilityBase Ability = game.getAbilities().get(Victim);
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c�ɷ�&f] &c" + Victim.getName() + "&f���� &e" + Ability.getAbilityName() + " &f�ɷ��̾����ϴ�!"));
					} else {
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c�ɷ�&f] &c" + Victim.getName() + "&f���� �ɷ��� �����ϴ�!"));
					}
				}
				
				if(AbilityWarSettings.getEliminate()) {
					Eliminate(Victim);
				}
			}
		}
	}
	
	/**
	 * Ż���� ���� �г��� ���
	 */
	private ArrayList<String> Eliminated = new ArrayList<String>();
	
	/**
	 * �÷��̾ Ż����ŵ�ϴ�.
	 * @param p   Ż����ų �÷��̾��Դϴ�.
	 */
	public void Eliminate(Player p) {
		Eliminated.add(p.getName());
		p.kickPlayer(
				ChatColor.translateAlternateColorCodes('&', "&2��&aAbilityWar&2��")
				+ "\n"
				+ ChatColor.translateAlternateColorCodes('&', "&fŻ���ϼ̽��ϴ�."));
		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c" + p.getName() + "&f���� Ż���ϼ̽��ϴ�."));
	}
	
	/**
	 * �÷��̾��� Ż�� ���θ� Ȯ���մϴ�.
	 */
	public boolean isEliminated(Player p) {
		return Eliminated.contains(p.getName());
	}
	
}
