package daybreak.abilitywar.game.manager.gui;

import com.google.common.collect.Table.Cell;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.game.manager.gui.tip.AbilityTipGUI;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.minecraft.item.builder.ItemBuilder;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

public class SynergyListGUI implements Listener, PagedGUI {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private static final Set<Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration>> synergies = new TreeSet<>(new Comparator<Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration>>() {
		@Override
		public int compare(Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration> o1, Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration> o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getValue().getManifest().name(), o2.getValue().getManifest().name());
		}
	});

	private static void checkCache() {
		final Set<Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration>> cellSet = SynergyFactory.cellSet();
		if (synergies.size() == cellSet.size()) return;
		synergies.clear();
		synergies.addAll(cellSet);
	}

	private final Plugin plugin;
	private final Player player;
	private int currentPage = 1;
	private Inventory gui;
	private CompletableFuture<Void> asyncWork;

	@Override
	public int getCurrentPage() {
		return currentPage;
	}

	public SynergyListGUI(Player player, Plugin plugin) {
		this.plugin = plugin;
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		checkCache();
	}

	@Override
	public void openGUI(int page) {
		if (asyncWork != null) {
			asyncWork.cancel(true);
			this.asyncWork = null;
		}
		int maxPage = ((synergies.size() - 1) / 36) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 54, "§0시너지 목록");
		this.currentPage = page;
		int count = 0;

		for (Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration> cell : synergies) {
			if (count / 36 == page - 1) {
				final AbilityRegistration registration = cell.getValue();
				final AbilityManifest manifest = registration.getManifest();
				final ItemStack stack = MaterialX.WHITE_STAINED_GLASS.createItem();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§b" + manifest.name());
				final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
				if (registration.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
				if (registration.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타게팅");
				if (registration.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
				final List<String> lore = Messager.asList(
						"§7" + cell.getRowKey().getManifest().name() + " §f+ §7" + cell.getColumnKey().getManifest().name(),
						"§f등급: " + manifest.rank().getRankName(),
						"§f종류: " + manifest.species().getSpeciesName(),
						joiner.toString(),
						"", "§7※ 팁을 보려면 클릭하세요.", "");
				for (final String line : registration.getManifest().explain()) {
					lore.add(ChatColor.WHITE.toString().concat(line));
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);
				gui.setItem(count % 36, stack);
			}
			count++;
		}

		{
			final int finalPage = page;
			this.asyncWork = CompletableFuture.runAsync(new Runnable() {
				@Override
				public void run() {
					int count = 0;

					for (Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration> cell : synergies) {
						if (count / 36 == finalPage - 1) {
							final AbilityRegistration registration = cell.getValue();
							final AbilityManifest manifest = registration.getManifest();
							final ItemStack stack = new ItemStack(Material.IRON_BLOCK);
							final ItemMeta meta = stack.getItemMeta();
							meta.setDisplayName("§b" + manifest.name());
							final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
							if (registration.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
							if (registration.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타게팅");
							if (registration.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
							final List<String> lore = Messager.asList(
									"§7" + cell.getRowKey().getManifest().name() + " §f+ §7" + cell.getColumnKey().getManifest().name(),
									"§f등급: " + manifest.rank().getRankName(),
									"§f종류: " + manifest.species().getSpeciesName(),
									joiner.toString(),
									"", "§7※ 팁을 보려면 클릭하세요.", "");
							for (final Iterator<String> iterator = AbilityBase.getExplanation(registration); iterator.hasNext();) {
								lore.add(ChatColor.WHITE.toString().concat(iterator.next()));
							}
							meta.setLore(lore);
							stack.setItemMeta(meta);
							if (currentPage != finalPage) break;
							gui.setItem(count % 36, stack);
						}
						count++;
					}
				}
			});
		}

		if (page > 1) {
			gui.setItem(48, PREVIOUS_PAGE);
		}

		if (page != maxPage) {
			gui.setItem(50, NEXT_PAGE);
		}

		final ItemStack stack = new ItemStack(Material.PAPER, 1);
		final ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		stack.setItemMeta(meta);
		gui.setItem(49, stack);

		player.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui)) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);
			final ItemStack currentItem = e.getCurrentItem();
			if (currentItem != null && currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				final String displayName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
				if (currentItem.getType() == Material.IRON_BLOCK || MaterialX.WHITE_STAINED_GLASS.compare(currentItem)) {
					final AbilityRegistration registration = AbilityFactory.getByName(displayName);
					if (registration != null) {
						new AbilityTipGUI(player, registration, this, plugin).openGUI(1);
					}
				} else {
					if (displayName.equals("이전 페이지")) {
						openGUI(currentPage - 1);
					} else if (displayName.equals("다음 페이지")) {
						openGUI(currentPage + 1);
					}
				}
			}
		}
	}

}