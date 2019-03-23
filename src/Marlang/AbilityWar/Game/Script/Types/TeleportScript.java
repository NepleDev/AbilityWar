package Marlang.AbilityWar.Game.Script.Types;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.Game.Games.AbstractGame;
import Marlang.AbilityWar.Game.Script.Objects.AbstractScript;
import Marlang.AbilityWar.Game.Script.Objects.SerializableLocation;

public class TeleportScript extends AbstractScript {
	
	private static final long serialVersionUID = -5963721016613765175L;
	
	private final SerializableLocation location;
	
	public TeleportScript(String ScriptName, int Time, boolean Loop, int LoopCount, String PreRunMessage, String RunMessage, Location location) {
		super(ScriptName, Time, Loop, LoopCount, PreRunMessage, RunMessage);
		this.location = new SerializableLocation(location);
	}
	
	@Override
	public void Execute(AbstractGame game) {
		try {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.teleport(location.getLocation());
			}
		} catch (NullPointerException pointerException) {}
	}
	
}