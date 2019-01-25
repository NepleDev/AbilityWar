package Marlang.AbilityWar.GameManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.EventExecutor;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.API.Events.AbilityWarJoinEvent;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent.Progress;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveClickType;
import Marlang.AbilityWar.Ability.AbilityBase.ActiveMaterialType;
import Marlang.AbilityWar.Config.AbilityWarSettings;

public class GameListener implements Listener, EventExecutor {
	
	private Game game;
	
	public GameListener(Game game) {
		this.game = game;
		
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		
		for(Class<? extends Event> e : PassiveEvents) {
			Bukkit.getPluginManager().registerEvent(e, this, EventPriority.HIGH, this, AbilityWar.getPlugin());
		}
	}

	/**
	 * 게임 종료시 Listener Unregister
	 */
	@EventHandler
	public void onGameProcess(AbilityWarProgressEvent e) {
		if(e.getProgress().equals(Progress.Game_ENDED)) {
			HandlerList.unregisterAll(this);
		}
	}
	
	private HashMap<String, Instant> InstantMap = new HashMap<String, Instant>();
	
	/**
	 * 액티브 Listener
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ActiveMaterialType mt = getMaterialType(p.getInventory().getItemInMainHand().getType());
		ActiveClickType ct = (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) ? ActiveClickType.RightClick : ActiveClickType.LeftClick;
		if(mt != null) {
			if(game.getAbilities().containsKey(p)) {
				AbilityBase Ability = game.getAbilities().get(p);
				if(!Ability.isRestricted()) {
					if(InstantMap.containsKey(p.getName())) {
						Instant Before = InstantMap.get(p.getName());
						Instant Now = Instant.now();
						long Duration = java.time.Duration.between(Before, Now).toMillis();
						if(Duration >= 250) {
							InstantMap.put(p.getName(), Instant.now());
							Ability.ActiveSkill(mt, ct);
						}
					} else {
						InstantMap.put(p.getName(), Instant.now());
						Ability.ActiveSkill(mt, ct);
					}
				}
			}
		}
	}
	
	/**
	 * 내구도 Listener
	 */
	@EventHandler
	public void onArmorDurabilityChange(PlayerItemDamageEvent e) {
		if(AbilityWarSettings.getInfiniteDurability()) {
			e.setCancelled(true);
			
			e.getItem().setDurability((short) 0);
		}
	}
	
	/**
	 * 날씨 Listener
	 */
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if(game.isGameStarted()) {
			if(AbilityWarSettings.getClearWeather()) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			if(game.getInvincibility().isTimerRunning()) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if(AbilityWarSettings.getNoHunger()) {
			e.setCancelled(true);
			
			Player p = (Player) e.getEntity();
			p.setFoodLevel(19);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player joined = e.getPlayer();
		
		ArrayList<Player> PlayersToRemove = new ArrayList<Player>();
		ArrayList<Player> PlayersToAdd = new ArrayList<Player>();
		
		for(Player p : game.getPlayers()) {
			if(p.getName().equals(joined.getName())) {
				PlayersToRemove.add(p);
				PlayersToAdd.add(joined);
			}
		}
		
		game.getPlayers().removeAll(PlayersToRemove);
		game.getPlayers().addAll(PlayersToAdd);
		
		ArrayList<Player> AbilitiesToRemove = new ArrayList<Player>();
		HashMap<Player, AbilityBase> AbilitiesToAdd = new HashMap<Player, AbilityBase>();
		
		for(Player p : game.getAbilities().keySet()) {
			if(p.getName().equals(joined.getName())) {
				AbilityBase Ability = game.getAbilities().get(p);
				Ability.setPlayer(joined);
				AbilitiesToRemove.add(p);
				AbilitiesToAdd.put(joined, Ability);
			}
		}
		
		game.getAbilities().keySet().removeAll(AbilitiesToRemove);
		game.getAbilities().putAll(AbilitiesToAdd);
		
		AbilitySelect select = game.getAbilitySelect();
		if(select != null) {
			ArrayList<Player> SelectToRemove = new ArrayList<Player>();
			HashMap<Player, Boolean> SelectToAdd = new HashMap<Player, Boolean>();
			
			for(Player p : select.AbilitySelect.keySet()) {
				if(p.getName().equals(joined.getName())) {
					SelectToRemove.add(p);
					SelectToAdd.put(joined, select.AbilitySelect.get(p));
				}
			}
			
			select.AbilitySelect.keySet().removeAll(SelectToRemove);
			select.AbilitySelect.putAll(SelectToAdd);
		}
		
		AbilityWarJoinEvent event = new AbilityWarJoinEvent(joined, game.getGameAPI());
		Bukkit.getPluginManager().callEvent(event);
	}
	
	private static ArrayList<Class<? extends Event>> PassiveEvents = new ArrayList<Class<? extends Event>>();
	
	static {
		registerPassive(EntityDamageEvent.class);
		registerPassive(ProjectileLaunchEvent.class);
		registerPassive(ProjectileHitEvent.class);
		registerPassive(BlockBreakEvent.class);
		registerPassive(BlockExplodeEvent.class);
		registerPassive(PlayerMoveEvent.class);
	}
	
	public static void registerPassive(Class<? extends Event> e) {
		if(!PassiveEvents.contains(e)) {
			PassiveEvents.add(e);
		}
	}
	
	public ActiveMaterialType getMaterialType(Material m) {
		for(ActiveMaterialType Type : ActiveMaterialType.values()) {
			if(Type.getMaterial().equals(m)) {
				return Type;
			}
		}
		
		return null;
	}

	@Override
	public void execute(Listener listener, Event e) throws EventException {
		for(AbilityBase Ability : game.getAbilities().values()) {
			if(!Ability.isRestricted()) {
				Ability.PassiveSkill(e);
			}
		}
	}
	
}
