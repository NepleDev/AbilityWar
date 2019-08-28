package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "심안", Rank = Rank.C, Species = Species.HUMAN)
public class DarkVision extends AbilityBase {
	
	public static SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(DarkVision.class, "Distance", 30,
			"# 거리 설정") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public DarkVision(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f앞이 보이지 않는 대신, 플레이어의 " + DistanceConfig.getValue() + "칸 안에 있는 플레이어들은"),
				ChatColor.translateAlternateColorCodes('&', "&f발광 효과가 적용됩니다. 또한, 빠르게 달리고 높게 점프할 수 있습니다."));
	}

	private TimerBase Dark = new TimerBase() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			EffectLib.BLINDNESS.addPotionEffect(getPlayer(), 40, 0, true);
			EffectLib.SPEED.addPotionEffect(getPlayer(), 40, 5, true);
			EffectLib.JUMP.addPotionEffect(getPlayer(), 40, 1, true);
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(2);
	
	private TimerBase Vision = new TimerBase() {
		
		Integer Distance = DistanceConfig.getValue();
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			for(Player p : LocationUtil.getNearbyPlayers(getPlayer(), Distance, Distance)) {
				EffectLib.GLOWING.addPotionEffect(p, 10, 0, true);
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(2);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	@Override
	public void onRestrictClear() {
		Dark.StartTimer();
		Vision.StartTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}

}