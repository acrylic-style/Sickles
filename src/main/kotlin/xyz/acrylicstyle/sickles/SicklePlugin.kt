package xyz.acrylicstyle.sickles

import net.azisaba.kotlinnmsextension.v1_20_R1.getBoolean
import net.azisaba.kotlinnmsextension.v1_20_R1.getOrCreateTag
import net.azisaba.kotlinnmsextension.v1_20_R1.set
import net.azisaba.kotlinnmsextension.v1_20_R1.tag
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.Damageable
import org.bukkit.plugin.java.JavaPlugin

class SicklePlugin : JavaPlugin(), Listener {
    private val recipes = mutableListOf<NamespacedKey>()

    override fun onEnable() {
        saveDefaultConfig()
        Bukkit.getPluginManager().registerEvents(this, this)
        run {
            val key = NamespacedKey(this, "wooden_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.WOODEN_HOE, "木の草刈り鎌", config.getInt("custom-model-data.wood", 1)))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', RecipeChoice.MaterialChoice(*Tag.PLANKS.values.toTypedArray()))
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
        run {
            val key = NamespacedKey(this, "stone_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.STONE_HOE, "石の草刈り鎌", config.getInt("custom-model-data.stone", 1)))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.COBBLESTONE)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
        run {
            val key = NamespacedKey(this, "iron_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.IRON_HOE, "鉄の草刈り鎌", config.getInt("custom-model-data.iron", 1)))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.IRON_INGOT)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
        run {
            val key = NamespacedKey(this, "golden_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.GOLDEN_HOE, "金の草刈り鎌", config.getInt("custom-model-data.gold", 1)))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.GOLD_INGOT)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
        run {
            val key = NamespacedKey(this, "emerald_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.IRON_HOE, "エメラルドの草刈り鎌", config.getInt("custom-model-data.emerald", 2)))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.EMERALD)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
        run {
            val key = NamespacedKey(this, "diamond_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.DIAMOND_HOE, "ダイヤの草刈り鎌", config.getInt("custom-model-data.diamond", 1)))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.DIAMOND)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
    }

    override fun onDisable() {
        recipes.forEach { key -> Bukkit.removeRecipe(key) }
        recipes.clear()
    }

    private var destroying = false

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        if (destroying) return
        destroying = true
        try {
            val item = e.player.inventory.itemInMainHand
            if (types.contains(e.block.type) && isSickle(item)) {
                val meta = item.itemMeta as Damageable
                var damage = meta.damage
                getNearbyBlocks(e.block.location, 5).forEach { block ->
                    if (!types.contains(block.type)) return@forEach
                    if (damage >= item.type.maxDurability) return@forEach
                    val d = 4.0 // lower value = less chance to break item
                    if (!BlockBreakEvent(block, e.player).apply {
                            Bukkit.getPluginManager().callEvent(this)
                        }.isCancelled) {
                        if (Math.random() > (meta.getEnchantLevel(Enchantment.DURABILITY) / d)) damage += 2
                        block.breakNaturally(ItemStack(Material.DIAMOND_HOE))
                    }
                }
                if (e.player.gameMode == GameMode.CREATIVE) return
                if (damage >= item.type.maxDurability) {
                    e.player.spawnParticle(Particle.ITEM_CRACK, e.player.location, 1, item)
                    e.player.playSound(e.player.location, Sound.ITEM_SHIELD_BREAK, 1F, 1F)
                    e.player.inventory.setItemInMainHand(null)
                    return
                }
                meta.damage = damage
                item.itemMeta = meta
            }
        } finally {
            destroying = false
        }
    }

    companion object {
        val types = mutableSetOf<Material>()

        init {
            types.add(Material.GRASS)
            types.add(Material.TALL_GRASS)
            types.add(Material.SEAGRASS)
            types.add(Material.TALL_SEAGRASS)
            types.add(Material.KELP)
            types.add(Material.KELP_PLANT)
            Tag.SAPLINGS.values.forEach { types.add(it) }
            Tag.SWORD_EFFICIENT.values.forEach { types.add(it) }
            Tag.TALL_FLOWERS.values.forEach { types.add(it) }
            Tag.FLOWERS.values.forEach { types.add(it) }
            Tag.SMALL_FLOWERS.values.forEach { types.add(it) }

            Tag.LEAVES.values.forEach { types.remove(it) }
        }

        fun isSickle(item: ItemStack?): Boolean =
            item != null && CraftItemStack.asNMSCopy(item).tag?.getBoolean("sickle") == true

        fun getSickleItem(material: Material, name: String, model: Int): ItemStack {
            val item = setSickle(ItemStack(material))
            val meta = item.itemMeta!!
            meta.setDisplayName("§f$name")
            meta.setCustomModelData(model)
            item.itemMeta = meta
            return item
        }

        private fun setSickle(item: ItemStack): ItemStack {
            val nms = CraftItemStack.asNMSCopy(item)
            val tag = nms.getOrCreateTag()
            tag["sickle"] = true
            tag["sSickle"] = "yes"
            nms.tag = tag
            return CraftItemStack.asBukkitCopy(nms)
        }

        fun getNearbyBlocks(location: Location, radius: Int): MutableList<Block> {
            val blocks = mutableListOf<Block>()
            for (x in location.blockX - radius..location.blockX + radius) {
                for (y in location.blockY - radius..location.blockY + radius) {
                    for (z in location.blockZ - radius..location.blockZ + radius) {
                        blocks.add(location.world!!.getBlockAt(x, y, z))
                    }
                }
            }
            return blocks
        }
    }
}
