package daybreak.abilitywar.utils.base.minecraft.nms.v1_13_R2;

import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.INMS;
import net.minecraft.server.v1_13_R2.EntityArmorStand;
import net.minecraft.server.v1_13_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayInClientCommand;
import net.minecraft.server.v1_13_R2.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_13_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_13_R2.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_13_R2.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NMSImpl implements INMS {

	@Override
	public void respawn(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
	}

	@Override
	public void clearTitle(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	}

	@Override
	public void sendActionbar(Player player, String string, int fadeIn, int stay, int fadeOut) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
		connection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.ACTIONBAR, ChatSerializer.a("{\"text\":\"" + string + "\"}"), fadeIn, stay, fadeOut));
	}

	@Override
	public float getAttackCooldown(Player player) {
		return ((CraftPlayer) player).getHandle().r(0f);
	}

	@Override
	public void rotateHead(Player receiver, Entity entity, float yaw, float pitch) {
		PlayerConnection connection = ((CraftPlayer) receiver).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutEntityTeleport(((CraftEntity) entity).getHandle()));
		byte fixedYaw = (byte) (yaw * (256F / 360F));
		connection.sendPacket(new PacketPlayOutEntityLook(entity.getEntityId(), fixedYaw, (byte) (pitch * (256F / 360F)), entity.isOnGround()));
		connection.sendPacket(new PacketPlayOutEntityHeadRotation(((CraftEntity) entity).getHandle(), fixedYaw));
	}

	@Override
	public IHologram newHologram(World world, double x, double y, double z, String text) {
		return new HologramImpl(world, x, y, z, text);
	}

	@Override
	public IHologram newHologram(World world, double x, double y, double z) {
		return new HologramImpl(world, x, y, z);
	}

	@Override
	public float getAbsorptionHearts(Player player) {
		return ((CraftPlayer) player).getHandle().getAbsorptionHearts();
	}

	@Override
	public void setAbsorptionHearts(Player player, float absorptionHearts) {
		((CraftPlayer) player).getHandle().setAbsorptionHearts(absorptionHearts);
	}

	@Override
	public void broadcastEntityEffect(Entity entity, byte status) {
		final net.minecraft.server.v1_13_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		nmsEntity.getWorld().broadcastEntityEffect(nmsEntity, status);
	}

	@Override
	public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
		((CraftEntity) entity).getHandle().setLocation(x, y, z, yaw, pitch);
	}

	@Override
	public void removeBoundingBox(ArmorStand armorStand) {
		final EntityArmorStand nmsArmorStand = ((CraftArmorStand) armorStand).getHandle();
		nmsArmorStand.getDataWatcher().set(EntityArmorStand.a, (byte) (nmsArmorStand.getDataWatcher().get(EntityArmorStand.a) | 16));
		nmsArmorStand.setSize(0F, 0F);
	}

}