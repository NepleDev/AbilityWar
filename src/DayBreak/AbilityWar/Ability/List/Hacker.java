package DayBreak.AbilityWar.Ability.List;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Library.ParticleLib.RGB;
import DayBreak.AbilityWar.Utils.Library.TItle.Title;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Math.Geometry.Circle;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "해커", Rank = Rank.A, Species = Species.HUMAN)
public class Hacker extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Hacker.class, "Cooldown", 180, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Hacker.class, "Duration", 5, 
			"# 능력 지속시간") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public Hacker(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 자신에게 제일 가까운 플레이어를 해킹해 좌표를 알아내고"),
				ChatColor.translateAlternateColorCodes('&', "&f" + DurationConfig.getValue() + "초간 해당 플레이어가 움직이지 못하게 합니다."),
				Messager.formatCooldown(CooldownConfig.getValue()));
	}

	private Player Target = null;
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	private final int DurationTick = DurationConfig.getValue() * 20;
	
	private TimerBase Skill = new TimerBase(100) {

		private int Count;
		
		@Override
		protected void onStart() {
			this.Count = 1;
		}
		
		@Override
		protected void onEnd() {
			if(Target != null) {
				new Title("", "", 0, 1, 0).sendTo(getPlayer());

				int X = (int) Target.getLocation().getX();
				int Y = (int) Target.getLocation().getY();
				int Z = (int) Target.getLocation().getZ();
				Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&e" + Target.getName() + "&f님은 &aX " + X + "&f, &aY " + Y + "&f, &aZ "+ Z + "&f에 있습니다."));

				new Title(ChatColor.translateAlternateColorCodes('&', "&5해킹당했습니다!"), "", 0, 40, 0).sendTo(Target);
				Hacker.this.getGame().getEffectManager().Stun(Target, DurationTick);
				Particle.StartTimer();
			}
		}
		
		@Override
		protected void TimerProcess(Integer Seconds) {
			if(Target != null) {
				StringBuilder sb = new StringBuilder();
				int all = 20;
				int green = (int)(((double) Count / 100) * all);
				for(int i = 0; i < green; i++) sb.append(ChatColor.GREEN + "|");
				int gray = all - green;
				for(int i = 0; i < gray; i++) sb.append(ChatColor.GRAY + "|");
				
				Title packet = new Title(ChatColor.translateAlternateColorCodes('&', "&e" + Target.getName() + " &f해킹중..."),
						ChatColor.translateAlternateColorCodes('&', sb.toString() + " &f" + Count + "%"), 0, 5, 0);
				packet.sendTo(getPlayer());
				
				Count++;
			}
		}
	}.setPeriod(1).setSilentNotice(true);
	
	private TimerBase Particle = new TimerBase(DurationTick) {

		private double y;
		private boolean add;
		
		@Override
		public void onStart() {
			y = 0.0;
			add = true;
		}

		private final int amount = 25;
		private final Circle top = new Circle(getPlayer().getLocation(), 1).setAmount(amount);
		private final Circle bottom = new Circle(getPlayer().getLocation(), 1).setAmount(amount);
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(Target != null) {
				if(add && y >= 2.0) {
					add = false;
				} else if(!add && y <= 0) {
					add = true;
				}
				
				if(add) {
					y += 0.1;
				} else {
					y -= 0.1;
				}
				
				List<Location> locations = new ArrayList<>();
				locations.addAll(top.setCenter(Target.getLocation().add(0, y, 0)).getLocations());
				locations.addAll(bottom.setCenter(Target.getLocation().add(0, 2.0 - y, 0)).getLocations());
				
				for(Location l : locations) ParticleLib.REDSTONE.spawnParticle(l, new RGB(168, 121, 171), 0);
			}
		}
		
		@Override
		public void onEnd() {
			Target = null;
		}
		
	}.setPeriod(1);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Player target = LocationUtil.getNearestPlayer(getPlayer());
					
					if(target != null) {
						Target = target;
						Skill.StartTimer();
						
						Cool.StartTimer();
						
						return true;
					} else {
						Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&a가장 가까운 플레이어&f가 존재하지 않습니다."));
					}
				}
			}
		}
		
		return false;
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
