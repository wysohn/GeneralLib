package org.generallib.pluginbase.manager

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.generallib.location.utils.LocationUtil
import org.generallib.pluginbase.PluginBase
import org.generallib.pluginbase.PluginManager
import org.generallib.pluginbase.constants.Area
import org.generallib.pluginbase.constants.SimpleLocation
import org.generallib.pluginbase.language.DefaultLanguages
import sun.plugin2.main.server.Plugin
import java.util.*

class AreaSelectionManager(base: PluginBase, priority: Int): PluginManager<PluginBase>(base, priority), Listener{
    val selecting = HashSet<UUID>();

    val selectingLeft = HashMap<UUID, SimpleLocation>();
    val selectingRight = HashMap<UUID, SimpleLocation>();

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    override fun onReload() {
    }

    /**
     * @return true if selection mode is turned on; false otherwise
     */
    fun toggleSelectionMode(player: UUID): Boolean{
        if(selecting.contains(player)){
            selecting.remove(player);
            resetSelections(player);
            return false;
        }else{
            selecting.add(player);
            return true;
        }
    }

    fun resetSelections(player: UUID){
        selecting.remove(player);
        selectingLeft.remove(player);
        selectingRight.remove(player);
    }

    fun getSelection(player: UUID): Area?{
        val left = selectingLeft[player];
        val right = selectingRight[player];

        if(left != null && right != null){
            if(left.world != right.world)
                return null;

            return Area.formAreaBetweenTwoPoints(left, right);
        } else {
            return null;
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent){
        selecting.remove(e.player.uniqueId);
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent){
        val player = e.player;
        val uuid = player.uniqueId;

        if(!selecting.contains(uuid))
            return;

        e.setCancelled(true);

        if(e.hand != EquipmentSlot.HAND)
            return;

        if(e.clickedBlock == null)
            return;

        val sloc = LocationUtil.convertToSimpleLocation(e.clickedBlock.location);
        val result: ClickResult = onClick(e.action, uuid, sloc);
        when(result){
            ClickResult.DIFFERENTWORLD ->
                base.sendMessage(player, DefaultLanguages.AreaSelectionManager_DIFFERENTWORLD);
            ClickResult.COMPLETE ->{
                val area = Area.formAreaBetweenTwoPoints(selectingLeft[uuid], selectingRight[uuid]);
                base.lang.addString(area.smallest.toString());
                base.lang.addString(area.largest.toString());
                base.sendMessage(player, DefaultLanguages.AreaSelectionManager_COMPLETE);
            }
            ClickResult.LEFTSET ->
                base.sendMessage(player, DefaultLanguages.AreaSelectionManager_LEFTSET);
            ClickResult.RIGHTSET ->
                base.sendMessage(player, DefaultLanguages.AreaSelectionManager_RIGHTSET);
            ClickResult.NONE -> {}//do nothing
        }
    }

    private fun onClick(action: Action, uuid: UUID, sloc: SimpleLocation): ClickResult {
        when(action){
            Action.LEFT_CLICK_BLOCK -> {
                selectingLeft[uuid] = sloc;
            }
            Action.RIGHT_CLICK_BLOCK -> {
                selectingRight[uuid] = sloc;
            }
            else -> return ClickResult.NONE;
        }

        val left = selectingLeft[uuid];
        val right = selectingRight[uuid];

        if(left != null && right != null){
            if(left.world != right.world)
                return ClickResult.DIFFERENTWORLD;

            return ClickResult.COMPLETE;
        } else if(left != null){
            return ClickResult.LEFTSET;
        } else if(right != null){
            return ClickResult.RIGHTSET;
        } else {
            return ClickResult.NONE;
        }
    }

    private enum class ClickResult{
        DIFFERENTWORLD, COMPLETE, LEFTSET, RIGHTSET, NONE;
    }
}