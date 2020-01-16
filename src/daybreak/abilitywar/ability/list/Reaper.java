package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.math.geometry.Circle;
import daybreak.abilitywar.utils.math.geometry.Line;
import daybreak.abilitywar.utils.versioncompat.NMSUtil;
import daybreak.abilitywar.utils.versioncompat.NMSUtil.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

@AbilityManifest(Name = "영혼수확자", Rank = AbilityManifest.Rank.A, Species = AbilityManifest.Species.HUMAN)
public class Reaper extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Reaper.class, "Cooldown", 140,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(Reaper.class, "Distance", 7,
			"# 거리 설정") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Reaper(AbstractGame.Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f생명체가 죽을 경우 그 자리에 60초간 영혼이 남으며,"),
				ChatColor.translateAlternateColorCodes('&', "&f가까이 가면 수확할 수 있습니다. 철괴 우클릭 시 수확한 영혼을 모두 방출해"),
				ChatColor.translateAlternateColorCodes('&', "&f7초간 주위를 떠돌게 합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f주위를 떠도는 영혼들은 생명체에 닿으면 해당 생명체에게 데미지를 주며,"),
				ChatColor.translateAlternateColorCodes('&', "&f7초 후에는 영혼들을 모두 온 사방으로 흩뿌리며 닿은 생명체들에게"),
				ChatColor.translateAlternateColorCodes('&', "&f큰 데미지를 줍니다. 스킬을 맞은 모든 적에게 구속 디버프를 부여하며"),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 좌클릭하면 수확한 영혼의 개수를 확인합니다."));
	}

	private static final RGB BLACK = RGB.of(1, 1, 1);
	private static final RGB SOUL_COLOUR = RGB.of(1, 17, 48);
	private static final Vector MULTIPLY = new Vector(0.1, 0.55, 0.1);

	private final int distance = DistanceConfig.getValue();

	private int soulCount = 0;

	private final Vectors wingVectors = new Circle(0.6, 20).getVectors();
	private Vectors souls;

	private int tempSoul;

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final Timer abilityOne = new Timer(140) {
		@Override
		protected void onStart() {
			tempSoul = soulCount;
			souls = new Vectors();
			Random random = new Random();
			for (int i = 0; i < tempSoul; i++) {
				souls.add(Vector.getRandom().multiply(new Vector(
						random.nextBoolean() ? distance : -distance,
						0,
						random.nextBoolean() ? distance : -distance
				)).setY(Math.random() * 1.4));
			}
			soulCount = 0;
		}

		@Override
		protected void onProcess(int count) {
			getPlayer().setVelocity(getPlayer().getVelocity().multiply(MULTIPLY));
			Location playerLocation = getPlayer().getLocation().clone().add(0, 1, 0);
			for (Location location : wingVectors.clone().rotateAroundAxisY(playerLocation.getYaw()).rotateAroundAxis(playerLocation.getDirection().clone().setY(0).normalize(), 52).getAsLocations(playerLocation)) {
				ParticleLib.REDSTONE.spawnParticle(location, BLACK);
			}
			for (Location location : wingVectors.clone().rotateAroundAxisY(playerLocation.getYaw()).rotateAroundAxis(playerLocation.getDirection().clone().setY(0).normalize(), -52).getAsLocations(playerLocation)) {
				ParticleLib.REDSTONE.spawnParticle(location, BLACK);
			}
			for (Location location : souls.rotateAroundAxisY(6).getAsLocations(playerLocation)) {
				ParticleLib.REDSTONE.spawnParticle(location, SOUL_COLOUR);
				for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(location, 1.5, 1.5)) {
					if (!getPlayer().equals(damageable)) {
						damageable.damage(3.5, getPlayer());
						if (damageable instanceof LivingEntity)
							PotionEffects.SLOW.addPotionEffect((LivingEntity) damageable, 60, 2, true);
					}
				}
			}
		}

		@Override
		protected void onEnd() {
			abilityTwo.startTimer();
		}
	}.setPeriod(1);

	private final Timer abilityTwo = new Timer(25) {
		int count;

		@Override
		protected void onStart() {
			count = 0;
		}

		@Override
		protected void onProcess(int seconds) {
			count++;
			getPlayer().setVelocity(getPlayer().getVelocity().multiply(MULTIPLY));
			Location playerLocation = getPlayer().getLocation().clone().add(0, 1, 0);
			for (Location location : souls.rotateAroundAxisY(6).getAsLocations(playerLocation)) {
				Location realLocation = new Line(location, playerLocation).setLocationAmount(25).getLocation(location, count);
				ParticleLib.REDSTONE.spawnParticle(realLocation, SOUL_COLOUR);
				for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(realLocation, 1.5, 1.5)) {
					if (!getPlayer().equals(damageable)) {
						damageable.damage(3.5, getPlayer());
						if (damageable instanceof LivingEntity)
							PotionEffects.SLOW.addPotionEffect((LivingEntity) damageable, 60, 2, true);
					}
				}
			}
		}

		@Override
		protected void onEnd() {
			abilityThree.startTimer();
		}
	}.setPeriod(1);

	private final Timer abilityThree = new Timer(20) {
		int count;
		ArrayList<Locations> locationsList;

		@Override
		protected void onStart() {
			count = 0;
			locationsList = new ArrayList<>();
			Random random = new Random();
			Location playerLocation = getPlayer().getLocation();
			for (int i = 0; i < tempSoul; i++) {
				locationsList.add(new Line(Vector.getRandom().multiply(new Vector(
						random.nextBoolean() ? distance * 2 : -distance * 2,
						random.nextBoolean() ? distance * 2 : -distance * 2,
						random.nextBoolean() ? distance * 2 : -distance * 2
				))).setLocationAmount(20).getLocations(playerLocation));
			}
		}

		@Override
		protected void onProcess(int seconds) {
			count++;
			for (Locations locations : locationsList) {
				Location location = locations.get(count);
				ParticleLib.REDSTONE.spawnParticle(location, SOUL_COLOUR);
				for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(location, 1.5, 1.5)) {
					if (!getPlayer().equals(damageable)) {
						damageable.damage(7, getPlayer());
						if (damageable instanceof LivingEntity)
							PotionEffects.SLOW.addPotionEffect((LivingEntity) damageable, 60, 2, true);
					}
				}
			}
		}
	}.setPeriod(1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (ct.equals(ClickType.LEFT_CLICK)) {
				getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&0수확한 영혼&f: " + soulCount + "개"));
			} else if (ct.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()
					&& !abilityOne.isRunning() && !abilityTwo.isRunning()) {
				abilityOne.startTimer();
				cooldownTimer.startTimer();
				return true;
			}
		}
		return false;
	}

	private final Vectors sphere = LocationUtil.getSphere(0.07, 4);

	private final Timer soulNotice = new Timer() {
		@Override
		protected void onProcess(int count) {
			if (!cooldownTimer.isRunning())
				NMSUtil.PlayerUtil.sendActionbar(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&f" + soulCount + " &0●"), 0, 3, 0);
		}
	}.setPeriod(1);

	@SubscribeEvent(onlyRelevant = true)
	private void onRestrictionClear(AbilityRestrictionClearEvent e) {
		soulNotice.startTimer();
	}

	@SubscribeEvent
	private void onPlayerDeath(PlayerDeathEvent e) {
		if (getGame().isParticipating(e.getEntity())) {
			Locations locations = sphere.getAsLocations(e.getEntity().getLocation().clone().add(0, 1, 0));
			new Timer(1200) {
				@Override
				protected void onProcess(int count) {
					for (Location location : locations) {
						ParticleLib.REDSTONE.spawnParticle(getPlayer(), location, SOUL_COLOUR);
						if (location.distanceSquared(getPlayer().getLocation()) <= 1.2) {
							stopTimer(false);
							soulCount += 40;
							Hologram hologram = new Hologram(e.getEntity().getLocation().clone(), ChatColor.translateAlternateColorCodes('&', "&f+ " + 40 + " &0●"));
							new Timer(4) {
								@Override
								protected void onStart() {
									hologram.display(getPlayer());
								}

								@Override
								protected void onProcess(int count) {
								}

								@Override
								protected void onEnd() {
									hologram.hide(getPlayer());
								}

								@Override
								protected void onSilentEnd() {
									hologram.hide(getPlayer());
								}
							}.startTimer();
							break;
						}
					}
				}
			}.setPeriod(1).startTimer();
		}
	}

	@SubscribeEvent
	private void onEntityDeath(EntityDeathEvent e) {
		Locations locations = sphere.getAsLocations(e.getEntity().getLocation().clone().add(0, 1, 0));
		new Timer(1200) {
			@Override
			protected void onProcess(int count) {
				for (Location location : locations) {
					ParticleLib.REDSTONE.spawnParticle(getPlayer(), location, SOUL_COLOUR);
					if (location.distanceSquared(getPlayer().getLocation()) <= 1.2) {
						stopTimer(false);
						int soulGain = (int) e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
						if (e.getEntity() instanceof Animals) soulGain /= 7;
						else if (e.getEntity() instanceof Monster) soulGain /= 5;
						soulCount += soulGain;
						Hologram hologram = new Hologram(e.getEntity().getLocation().clone(), ChatColor.translateAlternateColorCodes('&', "&f+ " + soulGain + " &0●"));
						new Timer(4) {
							@Override
							protected void onStart() {
								hologram.display(getPlayer());
							}

							@Override
							protected void onProcess(int count) {
							}

							@Override
							protected void onEnd() {
								hologram.hide(getPlayer());
							}

							@Override
							protected void onSilentEnd() {
								hologram.hide(getPlayer());
							}
						}.startTimer();
						break;
					}
				}
			}
		}.setPeriod(1).startTimer();
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {

	}

}