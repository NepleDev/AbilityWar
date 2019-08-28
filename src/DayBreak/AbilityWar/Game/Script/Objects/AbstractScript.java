package DayBreak.AbilityWar.Game.Script.Objects;

import org.bukkit.ChatColor;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

public abstract class AbstractScript {

	private final String scriptType;
	private final String name;
	private final int period;
	private final int loopCount;
	private final String preMessage;
	private final String runMessage;
	private transient TimerBase Timer;

	public AbstractScript(String name, int period, int loopCount, String preMessage, String runMessage) {
		this.scriptType = this.getClass().getName();
		this.name = name;
		this.period = period;
		this.loopCount = loopCount;
		this.preMessage = preMessage;
		this.runMessage = runMessage;
		this.Timer = newTimer();
	}

	private transient AbstractGame game;

	public void Start(AbstractGame game) {
		this.game = game;

		if (Timer != null) {
			Timer.StartTimer();
		} else {
			Timer = newTimer();
			Timer.StartTimer();
		}
	}

	private TimerBase newTimer() {
		return new TimerBase(period) {

			// count가 0이 되면 루프 종료
			// count가 0보다 작을 경우 무한루프
			int count = loopCount;

			@Override
			public void onStart() {
				if(count > 0) count--;
			}

			@Override
			public void TimerProcess(Integer Seconds) {
				String msg = getPreRunMessage(Seconds);

				if (!msg.equalsIgnoreCase("none")) {
					if (Seconds == (this.getMaxCount() / 2)) {
						Messager.broadcastMessage(msg);
					} else if (Seconds <= 5 && Seconds >= 1) {
						Messager.broadcastMessage(msg);
					}
				}
			}

			@Override
			public void onEnd() {
				Execute(game);

				String msg = getRunMessage();
				if (!msg.equalsIgnoreCase("none")) {
					Messager.broadcastMessage(msg);
				}

				if (isLoop()) {
					if(count > -1) {
						if(count > 0) {
							this.StartTimer();
						}
					} else {
						this.StartTimer();
					}
				}
			}

		};
	}

	public String getType() {
		return scriptType;
	}

	public String getName() {
		return name;
	}

	protected boolean isLoop() {
		return loopCount != 0;
	}

	protected TimerBase getTimer() {
		return Timer;
	}

	private String getPreRunMessage(Integer Time) {
		return ChatColor.translateAlternateColorCodes('&',
				preMessage.replaceAll("%Time%", Time.toString()).replaceAll("%ScriptName%", name));
	}

	private String getRunMessage() {
		return ChatColor.translateAlternateColorCodes('&', runMessage.replaceAll("%ScriptName%", name));
	}

	protected abstract void Execute(AbstractGame game);

}