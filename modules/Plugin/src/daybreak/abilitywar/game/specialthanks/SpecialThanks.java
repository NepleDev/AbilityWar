package daybreak.abilitywar.game.specialthanks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.MojangAPI;
import daybreak.abilitywar.utils.base.minecraft.SkinInfo;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import daybreak.abilitywar.utils.base.minecraft.item.builder.CustomSkullBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public class SpecialThanks {

	private static final Map<String, SkinInfo> skinInfos = new HashMap<>();
	private static final Inventory common = Bukkit.createInventory(null, 9);
	private static final Deque<String> queue = new LinkedList<>();
	private static final SimpleTimer loader = new SimpleTimer(TaskType.NORMAL, 1) {
		@Override
		protected void run(int count) {
			if (!queue.isEmpty()) {
				try {
					common.setItem(1, Skulls.createSkull(queue.remove()));
				} catch (NoSuchElementException e) {
					stop(false);
				}
			}
		}
		@Override
		protected void onEnd() {
			if (!queue.isEmpty()) {
				start();
			}
		}
	}.setInitialDelay(TimeUnit.SECONDS, 2);

	static {
		CompletableFuture.runAsync(new Runnable() {
			@Override
			public void run() {
				registerSkinInfo("새벽", "107f4338b6aa471087748b7ef8170414");
			}
		});
	}

	@Nullable
	public static SkinInfo getSkinInfo(String name) {
		return skinInfos.get(name);
	}

	private static void registerSkinInfo(String displayName, String uuid) {
		try {
			final HttpsURLConnection connection = (HttpsURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").openConnection();
			if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
				try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					final JsonObject properties = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("properties").get(0).getAsJsonObject();
					skinInfos.put(displayName, new SkinInfo(displayName, properties.get("value").getAsString(), properties.get("signature").getAsString()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final Category DEVELOPER = new Category("§3개발자", new CustomSkullBuilder("bd9f18c9d85f92f72f864d67c1367e9a45dc10f371549c46a4d4dd9e4f13ff4").build())
			.addSpecialThanks(
					new SpecialThank("레인스타", "f6cef0829b7e48c1a973532389b6e3e1",
							"§7레인스타님",
							"많은 신규 능력의 개발, 리메이크, 리워크 및 밸런스 조정에",
							"기여했습니다. 능력자 플러그인에 능력이 세 개만 있었던 초반부터",
							"도와주신 분입니다. 현재는 §a레인스타 애드온§f을 개발하고 있습니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("코크스", "ecb53e2ffdf34089ae3486cff3fc5f34",
							"§7코크스님",
							"다양한 능력들의 개발, 리메이크, 리워크 및 밸런스 조정에",
							"기여했습니다. 능력자 플러그인이 개발 초반부터 도와주신 분입니다.",
							"능력자 플러그인 최초의 애드온, §c코크스 애드온§f을 개발했습니다."
					)
			);

	private static final Category TESTER = new Category("§e테스터", new CustomSkullBuilder("b33598437e313329eb141a13e92d9b0349aabe5c6482a5dde7b73753634aba").build())
			.addSpecialThanks(
					new SpecialThank("루르", "2dcb3299e24049adb8bb554d862bd7be",
							"§7루르님",
							"능력 테스트 및 리뷰에 도움을 주셨습니다.",
							"때때로 자신의 컨셉에 잡아먹혀 헛소리를 하긴 하지만, 능력자",
							"개발에 많은 도움이 되었습니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("퀘네", "101ceb32a2bc4dbd9d32291c86b66eca",
							"§7퀘네님",
							"능력자 개발 중반 테스팅에 도움을 주신 분입니다.",
							"현재는 어디론가 사라져 보이지 않습니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("망고", "2b4af44c86434a2fa3b07a34f4406636",
							"§7망고님",
							"능력 테스트에 도움을 주셨습니다. 자신의 애드온을",
							"만들겠다며 도전했지만, 모종의 이유§8(§7귀찮음§8, §7어려움§8)§f로 인하여",
							"하나의 능력만 개발된 채 방치된 상태입니다. 언젠가는 개발 완료되어",
							"추천 애드온 목록에서 볼 수 있기를 기대합니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("엔샤", "588d15d1fdb54e2db039a47d994d8390",
							"§7엔샤님",
							"테스트 서버에 놀러오는 플레이어와 능력자 플러그인 테스터",
							"그 사이의 누군가입니다. 현재 마인크래프트에서 언택트 세계일주",
							"프로젝트를 진행하고 있습니다. 과연 올해 안에는 완료할 수 있을까요?"
					)
			)
			.addSpecialThanks(
					new SpecialThank("쿤이", "d520b367afc3425b978e4f0a2682a379",
							"§7쿤이님",
							"§8자칭 §f능력자 초창기부터 능력자 플러그인에 애정을 가지고 플레이했던",
							"유저입니다. 신규 게임 모드 테스팅에 도움을 주셨습니다. 최근에는",
							"능력자 서버팩을 제작하여 용돈을 벌고있다는 소문이 있습니다.",
							"§7한 마디§8: §f마크는 돈벌기 쉬운 게임입니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("시그", "59ca33cc6a974045aae405f171b1206a",
							"§7시그님",
							"§8귀여움을 담당하고 있는 테스터입니다.",
							"신규 게임 모드 테스팅에 도움을 주셨습니다.",
							"§7한 마디§8: §f시그다."
					)
			);

	private static final Category STREAMER = new Category("§a스트리머", new CustomSkullBuilder("148a8c55891dec76764449f57ba677be3ee88a06921ca93b6cc7c9611a7af").build())
			.addSpecialThanks(
					new SpecialThank("윤하루", "507fc49666fb43489200251f48bf4719",
							"§7윤하루님",
							"이 플러그인을 이용한 최초의 서버팩을 만드신 분입니다.",
							"슬프게도 현재는 림프종으로 인하여 투병 생활을 하시는 중입니다.",
							"빠른 쾌유를 빕니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("악어", "4cf9e977d664427694be4358b1794ea3",
							"§7악어님",
							"능력자 플러그인 최초의 게임 모드, 체인지 능력자 전쟁의",
							"기반이 된 컨텐츠를 진행한 스트리머입니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("메이", "5ad79e2f720744bfbdcbf0dcc82691e2",
							"§7메이님",
							"귀여움을 담당하고 있는 스트리머입니다.",
							"능력자 플러그인을 이용해 재밌는 방송을 진행해주셨습니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("리더", "25cc5950b380499dbe681817b7d40a37",
							"§7리더님",
							"리더쉽과 통솔력을 담당하고 있는 스트리머입니다.",
							"능력자 플러그인으로 재미있고 활기찬 방송을 진행해주셨습니다.",
							"§8새벽이 방송에 등장할 때마다 시청자들에게 인사를 시키는..."
					)
			)
			.addSpecialThanks(
					new SpecialThank("김종겜", "e21490bf19e64fcc8d3c763ed3bff789",
							"§7김종겜님",
							"능력자 플러그인을 이용해 독보적인 시스템을 개발하여",
							"어디서도 볼 수 없었던 도시능력자를 만들어낸 스트리머입니다.",
							"또한, 신들의 전쟁이라는 독특한 컨텐츠를 개발하기도 했습니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("울산광부", "3298b2aafce340189654b2a76ce8c471",
							"§7울산광부님",
							"세계 최초 광질을 잘하는 스트리머!",
							"능력자 플러그인을 이용해 재밌고 활기찬 방송을 진행해주셨습니다.",
							"블라인드 게임 모드를 주로 즐기십니다."
					)
			)
			.addSpecialThanks(
					new SpecialThank("심승훈", "b24e310e236e453ba97e364097ad45c3",
							"§7심승훈님",
							"매 방학마다 도시 능력자라는 컨텐츠를 이용하여 어린 친구들의",
							"동심을 자극하는 좀비호러로맨스몹크리쳐생존정치 장르의 방송을",
							"이끌고 있는 스트리머입니다. 도시능력자로 밥줄을 유지합니다.",
							"§7한 마디§8: §f항상 잘 즐기고 있습니다!"
					)
			)
			.addSpecialThanks(
					new SpecialThank("빠이어곰", "53a21633738243ae9f7956b67ffed044",
							"§7빠이어곰님"
					)
			);

	public static final Category[] categories = {
			DEVELOPER, TESTER, STREAMER
	};

	private SpecialThanks() {
	}

	public static class Category {

		private final String displayName;
		private final ItemStack icon;
		private final List<SpecialThank> specialThanks = new ArrayList<>();

		public Category(final String displayName, final ItemStack icon) {
			this.displayName = displayName;
			this.icon = icon;
		}

		public String getDisplayName() {
			return displayName;
		}

		public ItemStack getIcon() {
			return icon.clone();
		}

		public Category addSpecialThanks(final SpecialThank specialThank) {
			specialThanks.add(specialThank);
			return this;
		}

		public List<SpecialThank> getSpecialThanks() {
			return specialThanks;
		}
	}

	public static class SpecialThank {

		private final String displayName;
		private final String[] description;
		private String name;

		public SpecialThank(String displayName, String uuid, String... description) {
			this.displayName = displayName;
			CompletableFuture.runAsync(new Runnable() {
				@Override
				public void run() {
					try {
						SpecialThank.this.name = MojangAPI.getNickname(uuid);
						queue.add(SpecialThank.this.name);
						loader.start();
						registerSkinInfo(displayName, uuid);
					} catch (IOException e) {
						SpecialThank.this.name = null;
					}
				}
			});
			this.description = new String[description.length];
			for (int i = 0; i < description.length; i++) {
				this.description[i] = ChatColor.WHITE + description[i];
			}
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getName() {
			return name;
		}

		public String[] getDescription() {
			return description;
		}
	}

}
