package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.SubscribeEvent;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.VersionCompat.VersionUtil;

@AbilityManifest(Name = "추적자", Rank = Rank.D, Species = Species.HUMAN)
public class Chaser extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Chaser.class, "Cooldown", 120,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Chaser(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f상대방을 철괴로 타격하면 대상에게 추적 장치를 부착합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f이후 철괴를 우클릭하면 추적 장치를 부착한 플레이어의 좌표를 알 수 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f추적 장치는 한명에게만 부착할 수 있습니다."));
	}

	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	private Player target = null;
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.LeftClick)) {
				Cool.isCooldown();
			} else if(ct.equals(ClickType.RightClick)) {
				if(target != null) {
					int X = (int) target.getLocation().getX();
					int Y = (int) target.getLocation().getY();
					int Z = (int) target.getLocation().getZ();
					
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&e" + target.getName() + "&f님은 &aX " + X + "&f, &aY " + Y + "&f, &aZ " + Z + "&f에 있습니다."));
				} else {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&f아무에게도 추적 장치를 부착하지 않았습니다. &8( &7추적 불가능 &8)"));
				}
			}
		}
		
		return false;
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getDamager().equals(getPlayer())) {
			if(e.getEntity() instanceof Player) {
				if(!e.isCancelled()) {
					if(VersionUtil.getItemInHand(getPlayer()).getType().equals(Material.IRON_INGOT)) {
						if(!Cool.isCooldown()) {
							Player p = (Player) e.getEntity();
							this.target = p;
							Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 추적 장치를 부착하였습니다."));
							
							Cool.StartTimer();
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}