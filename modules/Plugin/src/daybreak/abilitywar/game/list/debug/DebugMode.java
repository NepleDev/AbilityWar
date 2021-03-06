package daybreak.abilitywar.game.list.debug;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Category;
import daybreak.abilitywar.game.Category.GameCategory;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.DummyManager;
import daybreak.abilitywar.game.module.InfiniteDurability;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@GameManifest(name = "Debug Mode", description = {})
@Category(GameCategory.DEBUG)
public class DebugMode extends Game implements DefaultKitHandler, AbstractGame.Observer {

	public DebugMode() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		attachObserver(this);
		addModule(new DummyManager(this));
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void progressGame(int seconds) {
		if (seconds == 1) {
			setRestricted(false);
			if (Settings.getNoHunger()) {
				Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
			} else {
				Bukkit.broadcastMessage("§4배고픔 무제한§c이 적용되지 않습니다.");
			}
			if (Settings.getInfiniteDurability()) {
				addModule(new InfiniteDurability());
			} else {
				Bukkit.broadcastMessage("§4내구도 무제한§c이 적용되지 않습니다.");
			}
			startGame();
		}
	}

	@Override
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new DebugManagement(players);
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

	private class DebugParticipant extends ParticipantImpl {
		protected DebugParticipant(@NotNull Player player) {
			super(player);
		}
	}

	private class DebugManagement implements ParticipantStrategy {

		private final Map<UUID, Participant> participants = new HashMap<>();

		public DebugManagement(Collection<Player> players) {
			for (Player player : players) {
				participants.put(player.getUniqueId(), new DebugParticipant(player));
			}
		}

		@Override
		public Collection<? extends Participant> getParticipants() {
			return Collections.unmodifiableCollection(participants.values());
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid);
		}

		@Override
		public Participant getParticipant(UUID uuid) {
			return participants.get(uuid);
		}

		@Override
		public void addParticipant(Player player) throws UnsupportedOperationException {
			participants.putIfAbsent(player.getUniqueId(), new DebugParticipant(player));
		}

		@Override
		public void removeParticipant(UUID uuid) throws UnsupportedOperationException {
			participants.remove(uuid);
		}

	}

}
