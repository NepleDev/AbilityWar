package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.AbstractGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameReadyEvent extends GameEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public GameReadyEvent(AbstractGame game) {
		super(game);
	}

}
