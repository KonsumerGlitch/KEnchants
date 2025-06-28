package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SwordDanceEnchant extends CustomEnchant {

    private final Random random = new Random();

    // Trackers
    private static final ConcurrentHashMap<UUID, List<ArmorStand>> activeSwords = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, BukkitTask> animationTasks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Double> damageMultipliers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, double[]> baseAngles = new ConcurrentHashMap<>();

    public SwordDanceEnchant() {
        super("sworddance", "Sword Dance", EnchantmentRarity.LEGENDARY, 4);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
                Material.IRON_SWORD, Material.GOLDEN_SWORD,
                Material.STONE_SWORD, Material.WOODEN_SWORD
        );
    }

    @Override
    public String getDescription() {
        return "Chance to perform the sword dance boosting outgoing damage by 5–10%";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        UUID id = attacker.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(id) && now - cooldowns.get(id) < 25_000) return;

        if (random.nextDouble() < 0.6 * level) {
            activate(attacker, level);
            cooldowns.put(id, now);
        }
    }

    private void activate(Player player, int level) {
        UUID id = player.getUniqueId();
        cleanup(id);

        int count = Math.min(2 + (level - 1), 5);
        double multiplier = 1.0 + (0.05 + (level - 1) * 0.0167);
        damageMultipliers.put(id, multiplier);

        // Precompute uniform base angles
        double[] angles = new double[count];
        for (int i = 0; i < count; i++) {
            angles[i] = i * (2 * Math.PI / count);
        }
        baseAngles.put(id, angles);

        player.sendMessage("§6 §lSword Dance activated! §7(" +
                String.format("%.1f", (multiplier - 1) * 100) + "% damage boost)");

        List<ArmorStand> swords = spawnSwords(player, count);
        activeSwords.put(id, swords);
        animationTasks.put(id, animate(player, swords, angles));

        Bukkit.getPluginManager().getPlugin("KEnchantments")
                .getServer().getScheduler()
                .runTaskLater(Bukkit.getPluginManager().getPlugin("KEnchantments"),
                        () -> {
                            cleanup(id);
                            player.sendMessage("§7 Sword Dance ended.");
                        }, 200L);
    }

    private List<ArmorStand> spawnSwords(Player player, int count) {
        Location center = player.getLocation().add(0, 1.5, 0);
        List<ArmorStand> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ArmorStand as = (ArmorStand) player.getWorld().spawnEntity(center, EntityType.ARMOR_STAND);
            as.setVisible(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setSmall(true);
            as.setArms(true);
            as.setBasePlate(false);
            as.setMarker(true);

            // Equip sword and set arm pose to point it straight upward
            as.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            as.setRightArmPose(new EulerAngle(-Math.PI / 2, 0, 0));

            list.add(as);
        }
        return list;
    }

    private BukkitTask animate(Player player, List<ArmorStand> swords, double[] angles) {
        return new BukkitRunnable() {
            int tick = 0;
            public void run() {
                if (!player.isOnline() || player.isDead()) { cancel(); return; }
                Location base = player.getLocation().add(0, 1.5, 0);
                double radius = 1.0;
                double yOffset = 0.2 * Math.sin(tick * 0.1);  // Gentle up/down oscillation

                for (int i = 0; i < swords.size(); i++) {
                    ArmorStand s = swords.get(i);
                    double angle = angles[i] + tick * 0.05;  // Uniform rotation speed
                    double x = base.getX() + Math.cos(angle) * radius;
                    double z = base.getZ() + Math.sin(angle) * radius;
                    Location loc = new Location(base.getWorld(), x, base.getY() + yOffset, z);
                    loc.setDirection(base.toVector().subtract(loc.toVector()).normalize());
                    s.teleport(loc);

                    if (tick % 4 == 0) {
                        base.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 2, 0.05, 0.05, 0.05, 0.01);
                    }
                }

                if (tick % 6 == 0) {
                    base.getWorld().spawnParticle(Particle.CRIT_MAGIC, base, swords.size(), 0.2, 0.2, 0.2, 0.02);
                }

                tick++;
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("KEnchantments"), 0L, 1L);
    }

    private void cleanup(UUID id) {
        damageMultipliers.remove(id);
        baseAngles.remove(id);

        BukkitTask task = animationTasks.remove(id);
        if (task != null) task.cancel();

        List<ArmorStand> list = activeSwords.remove(id);
        if (list != null) {
            list.forEach(e -> { if (!e.isDead()) e.remove(); });
        }
    }

    public static boolean hasActiveSwordDance(UUID id) {
        return damageMultipliers.containsKey(id);
    }

    public static double getDamageMultiplier(UUID id) {
        return damageMultipliers.getOrDefault(id, 1.0);
    }

    public static void cleanupAll() {
        animationTasks.values().forEach(BukkitTask::cancel);
        animationTasks.clear();
        activeSwords.values().forEach(list -> list.forEach(e -> { if (!e.isDead()) e.remove(); }));
        activeSwords.clear();
        damageMultipliers.clear();
        cooldowns.clear();
        baseAngles.clear();
    }
}
