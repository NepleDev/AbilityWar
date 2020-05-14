package daybreak.abilitywar.game.list.mixability.changemix;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.ChangeAbilityWarSettings;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.decorator.Winnable;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.list.mixability.Mix;
import daybreak.abilitywar.game.list.mixability.MixAbility;
import daybreak.abilitywar.game.list.mixability.synergy.Synergy;
import daybreak.abilitywar.game.list.mixability.synergy.SynergyFactory;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

@GameManifest(Name = "체인지! 믹스 전쟁", Description = {"§f믹스 능력자 전쟁, 그리고 체인지! 능력자 전쟁을 동시에 즐겨보세요."})
@Beta
public class ChangeMix extends MixAbility implements DefaultKitHandler, Observer, Winnable {

	private final boolean invincible = Settings.InvincibilitySettings.isEnabled();

	@SuppressWarnings("deprecation")
	private final Objective lifeObjective = ServerVersion.getVersionNumber() >= 13 ?
			getScoreboardManager().getScoreboard().registerNewObjective("생명", "dummy", ChatColor.translateAlternateColorCodes('&', "&c생명"))
			: getScoreboardManager().getScoreboard().registerNewObjective("생명", "dummy");

	private final MixAbilityChanger changer = new MixAbilityChanger(this);
	private final InfiniteDurability infiniteDurability = new InfiniteDurability();
	private final int maxLife;
	private final Set<Participant> noLife = new HashSet<>();

	public ChangeMix() {
		setRestricted(invincible);
		this.maxLife = ChangeAbilityWarSettings.getLife();
		attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void progressGame(int Seconds) {
		switch (Seconds) {
			case 1:
				List<String> lines = Messager.asList(ChatColor.translateAlternateColorCodes('&', "&d==== &f게임 참여자 목록 &d===="));
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add(ChatColor.translateAlternateColorCodes('&', "&5" + count + ". &f" + p.getPlayer().getName()));
				}
				lines.add(ChatColor.translateAlternateColorCodes('&', "&f총 인원수 &5: &d" + count + "명"));
				lines.add(ChatColor.translateAlternateColorCodes('&', "&d=========================="));

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				if (getParticipants().size() < 2) {
					stop();
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. &8(&72명&8)"));
				}
				break;
			case 3:
				ArrayList<String> msg = new ArrayList<>();
				msg.add(ChatColor.translateAlternateColorCodes('&', "&5&l체인지! &d&l믹스 &f&l전쟁"));
				msg.add(ChatColor.translateAlternateColorCodes('&', "&e플러그인 버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()));
				msg.add(ChatColor.translateAlternateColorCodes('&', "&b모드 개발자 &7: &fDaybreak 새벽"));
				msg.add(ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &f새벽&7#5908"));

				GameCreditEvent event = new GameCreditEvent();
				Bukkit.getPluginManager().callEvent(event);

				msg.addAll(event.getCreditList());

				for (String m : msg) {
					Bukkit.broadcastMessage(m);
				}
				break;
			case 5:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인에 총 &d" + AbilityList.nameValues().size() + "개&f의 능력이 등록되어 있습니다."));
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임 시작시 &f첫번째 능력&7이 할당되며, 이후 &f" + TimeUtil.parseTimeAsString(changer.getPeriod()) + "&7마다 능력이 변경됩니다."));
				break;
			case 7:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7스코어보드 &f설정중..."));
				lifeObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				if (ServerVersion.getVersionNumber() < 13)
					lifeObjective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c생명"));
				for (Participant p : getParticipants()) {
					Score score = lifeObjective.getScore(p.getPlayer().getName());
					score.setScore(maxLife);
				}
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&d잠시 후 &f게임이 시작됩니다."));
				break;
			case 9:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &55&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 10:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &54&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 11:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &53&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &52&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &51&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 14:
				for (String m : new String[]{
						ChatColor.translateAlternateColorCodes('&', "&d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
						ChatColor.translateAlternateColorCodes('&', "&f                &5&l체인지! &d&l믹스 &f&l전쟁"),
						ChatColor.translateAlternateColorCodes('&', "&f                    게임 시작                "),
						ChatColor.translateAlternateColorCodes('&', "&d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")}) {
					Bukkit.broadcastMessage(m);
				}
				SoundLib.ENTITY_WITHER_SPAWN.broadcastSound();

				giveDefaultKit(getParticipants());

				for (Participant p : getParticipants()) {
					if (Settings.getSpawnEnable()) {
						p.getPlayer().teleport(Settings.getSpawnLocation());
					}
				}

				if (Settings.getNoHunger()) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&2배고픔 무제한&a이 적용됩니다."));
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
				}

				if (invincible) {
					getInvincibility().Start(false);
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
					setRestricted(false);
				}

				if (Settings.getInfiniteDurability()) {
					attachObserver(infiniteDurability);
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4내구도 무제한&c이 적용되지 않습니다."));
				}

				for (World w : Bukkit.getWorlds()) {
					if (Settings.getClearWeather()) {
						w.setStorm(false);
					}
				}

				changer.start();

				startGame();
				break;
		}
	}

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (isParticipating(player)) {
			Participant quitParticipant = getParticipant(player);
			Score score = lifeObjective.getScore(player.getName());
			if (score.isScoreSet()) {
				score.setScore(0);
				noLife.add(quitParticipant);
				getDeathManager().Operation(quitParticipant);

				Participant winner = null;
				int count = 0;
				for (Participant participant : getParticipants()) {
					if (!noLife.contains(participant)) {
						count++;
						winner = participant;
					}
				}

				if (count == 1) {
					Win(winner);
				}
			}
		}
	}

	@Override
	public DeathManager newDeathManager() {
		return new DeathManager(this) {
			@Override
			public void Operation(Participant victim) {
				Player victimPlayer = victim.getPlayer();
				Score score = lifeObjective.getScore(victimPlayer.getName());
				if (score.isScoreSet()) {
					int life = score.getScore();
					if (life >= 1) {
						score.setScore(--life);
						if (maxLife <= 10) {
							victimPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f남은 생명: &c" + Strings.repeat("&c♥", life) + Strings.repeat("&c♡", maxLife - life)));
						} else {
							victimPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f남은 생명: &c" + life));
						}
					}
					if (score.getScore() <= 0) {
						noLife.add(victim);
						super.Operation(victim);

						Participant winner = null;
						int count = 0;
						for (Participant participant : getParticipants()) {
							if (!noLife.contains(participant)) {
								count++;
								winner = participant;
							}
						}

						if (count == 1) {
							Win(winner);
						}
					}
				}
			}

			@Override
			protected String getRevealMessage(Participant victim) {
				Mix mix = (Mix) victim.getAbility();
				if (mix.hasAbility()) {
					if (mix.hasSynergy()) {
						Synergy synergy = mix.getSynergy();
						Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(synergy.getRegistration());
						String name = synergy.getName() + " (" + base.getLeft().getManifest().name() + " + " + base.getRight().getManifest().name() + ")";
						return ChatColor.translateAlternateColorCodes('&',
								"&f[&c능력&f] &c" + victim.getPlayer().getName() + "&f님의 능력은 &e" + name + "&f" + KoreanUtil.getJosa(name, KoreanUtil.Josa.이었였) + "습니다.");
					} else {
						String name = mix.getFirst().getName() + " + " + mix.getSecond().getName();
						return ChatColor.translateAlternateColorCodes('&',
								"&f[&c능력&f] &c" + victim.getPlayer().getName() + "&f님의 능력은 &e" + name + "&f" + KoreanUtil.getJosa(name, KoreanUtil.Josa.이었였) + "습니다.");
					}
				} else {
					return ChatColor.translateAlternateColorCodes('&',
							"&f[&c능력&f] &c" + victim.getPlayer().getName() + "&f님은 능력이 없습니다.");
				}
			}
		};
	}

	@Override
	public AbilitySelect newAbilitySelect() {
		return null;
	}

	@Override
	protected void onEnd() {
		lifeObjective.unregister();
		super.onEnd();
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

}
