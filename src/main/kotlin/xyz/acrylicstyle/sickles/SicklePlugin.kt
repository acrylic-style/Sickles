package xyz.acrylicstyle.sickles

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import util.CollectionList
import xyz.acrylicstyle.paper.Paper
import xyz.acrylicstyle.tomeito_api.utils.Log

class SicklePlugin : JavaPlugin(), Listener {
    private val recipes = CollectionList<NamespacedKey>()

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        run {
            val key = NamespacedKey(this, "wooden_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.WOODEN_HOE, "木の草刈り鎌"))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.OAK_PLANKS)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
        run {
            val key = NamespacedKey(this, "stone_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.STONE_HOE, "石の草刈り鎌"))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.COBBLESTONE)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
        run {
            val key = NamespacedKey(this, "iron_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.IRON_HOE, "鉄の草刈り鎌"))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.IRON_INGOT)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
        run {
            val key = NamespacedKey(this, "golden_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.GOLDEN_HOE, "金の草刈り鎌"))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.GOLD_INGOT)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
        run {
            val key = NamespacedKey(this, "diamond_sickle")
            val recipe = ShapedRecipe(key, getSickleItem(Material.DIAMOND_HOE, "ダイヤの草刈り鎌"))
            recipe.shape(" X ", "  X", "YX ")
            recipe.setIngredient('X', Material.DIAMOND)
            recipe.setIngredient('Y', Material.STICK)
            recipes.add(key)
            Bukkit.addRecipe(recipe)
        }
    }

    override fun onDisable() {
        recipes.forEach { key -> Bukkit.removeRecipe(key) }
        Log.info("Unregistered ${recipes.size} recipes.")
        recipes.clear()
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val item = e.player.inventory.itemInMainHand
        if (types.contains(e.block.type) && isSickle(item)) {
            val meta = item.ensureServerConversions().itemMeta as Damageable
            var damage = meta.damage
            getNearbyBlocks(e.block.location, 5).forEach { block ->
                if (!types.contains(block.type)) return@forEach
                if (damage >= item.type.maxDurability) return@forEach
                val d = 4.0 // lower value = less chance to break item
                if (Math.random() > (item.itemMeta.getEnchantLevel(Enchantment.DURABILITY)/d)) damage += 2
                block.breakNaturally()
            }
            if (damage >= item.type.maxDurability) {
                e.player.spawnParticle(Particle.ITEM_CRACK, e.player.location, 1, item)
                e.player.playSound(e.player.location, Sound.ITEM_SHIELD_BREAK, 1F, 1F)
                e.player.inventory.setItemInMainHand(null)
                return
            }
            meta.damage = damage
            item.itemMeta = meta as ItemMeta
        }
    }

    companion object {
        val types = CollectionList<Material>()

        init {
            types.add(Material.GRASS)
            types.add(Material.TALL_GRASS)
            types.add(Material.SEAGRASS)
            types.add(Material.TALL_SEAGRASS)
            types.add(Material.KELP)
            types.add(Material.KELP_PLANT)
        }

        fun isSickle(item: ItemStack?): Boolean = item != null && Paper.itemStack(item).orCreateTag.getBoolean("sickle")

        fun getSickleItem(material: Material, name: String): ItemStack {
            val item = setSickle(ItemStack(material))
            val meta = item.itemMeta
            val text = TextComponent(name)
            text.isItalic = false
            meta.setDisplayNameComponent(arrayOf(text))
            item.itemMeta = meta
            return item
        }

        private fun setSickle(item: ItemStack): ItemStack {
            val itemStack = Paper.itemStack(item)
            val tag = itemStack.orCreateTag
            tag.setBoolean("sickle", true)
            tag.setString("sSickle", "yes")
            itemStack.tag = tag
            return itemStack.itemStack
        }

        fun getNearbyBlocks(location: Location, radius: Int): CollectionList<Block> {
            val blocks = CollectionList<Block>()
            for (x in location.blockX - radius..location.blockX + radius) {
                for (y in location.blockY - radius..location.blockY + radius) {
                    for (z in location.blockZ - radius..location.blockZ + radius) {
                        blocks.add(location.world.getBlockAt(x, y, z))
                    }
                }
            }
            return blocks
        }
    }
}