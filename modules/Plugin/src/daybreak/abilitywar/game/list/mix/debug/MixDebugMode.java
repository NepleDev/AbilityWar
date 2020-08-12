package daybreak.abilitywar.game.list.mix.debug;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Category;
import daybreak.abilitywar.game.Category.GameCategory;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.list.mix.AbstractMix;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

@GameManifest(name = "Mix Debug Mode", description = {})
@Category(GameCategory.DEBUG)
public class MixDebugMode extends AbstractMix implements AbstractGame.Observer {

	public MixDebugMode() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void progressGame(int seconds) {
		if (seconds == 1) {
			setRestricted(false);
			startGame();
		}
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

}
