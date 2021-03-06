package org.generallib.pluginbase.manager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.generallib.database.Database;
import org.generallib.database.serialize.Serializer;
import org.generallib.item.utils.InventoryUtil;
import org.generallib.location.utils.LocationUtil;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.PluginLanguage.Language;
import org.generallib.pluginbase.constants.Area;
import org.generallib.pluginbase.constants.SimpleChunkLocation;
import org.generallib.pluginbase.constants.SimpleLocation;
import org.generallib.pluginbase.constants.structure.Structure;
import org.generallib.pluginbase.constants.structure.interfaces.filter.EntityFilter;
import org.generallib.pluginbase.constants.structure.interfaces.interact.ClickableLeft;
import org.generallib.pluginbase.constants.structure.interfaces.interact.ClickablePhysics;
import org.generallib.pluginbase.constants.structure.interfaces.interact.ClickableRight;
import org.generallib.pluginbase.constants.structure.interfaces.interact.ClickableRightShift;
import org.generallib.pluginbase.constants.structure.interfaces.ticking.Tickable;
import org.generallib.pluginbase.constants.structure.interfaces.trigger.EntityTrackingRegionTrigger;
import org.generallib.pluginbase.constants.structure.interfaces.trigger.RegionTrigger;
import org.generallib.pluginbase.language.DefaultLanguages;
import org.generallib.pluginbase.manager.event.PlayerBlockLocationEvent;

import copy.com.google.gson.JsonDeserializationContext;
import copy.com.google.gson.JsonElement;
import copy.com.google.gson.JsonObject;
import copy.com.google.gson.JsonParseException;
import copy.com.google.gson.JsonSerializationContext;

public class StructureManager extends ElementCachingManager<SimpleLocation, Structure> implements Listener {
    private static final String STRUCTURE_TAG = ChatColor.AQUA+"[Structure]";

    private final String STRUCTUREPACKAGEPATH;

    private final Map<SimpleChunkLocation, Set<SimpleLocation>> structuresInChunk = new ConcurrentHashMap<>();

    private final List<PermissionHandler> permissionHandlers = new ArrayList<>();
    private ItemDescriptionHandler itemDescriptionHandler;
    private Structure.UUIDToNameParser uuidToNameParse;
    private Structure.NameToUUIDParser nameToUUIDParse;

    private final Map<UUID, SimpleLocation> entityLocationMap = new HashMap<>();
    private final Map<UUID, WeakReference<Entity>> entityTrackMap = new ConcurrentHashMap<>();

    private long tick = 0;
    public StructureManager(PluginBase base, int loadPriority, String structureClassParentPath) {
        super(base, loadPriority);
        if(structureClassParentPath == null)
            throw new RuntimeException("structureClassParentPath must be set in order for StructureManager " +
                    "to work properly!");

        STRUCTUREPACKAGEPATH = structureClassParentPath;
    }

    @Override
    protected String getTableName() {
        return "Structures";
    }

    @Override
    protected Type getType() {
        return Structure.class;
    }

    @Override
    protected SimpleLocation createKeyFromString(String str) {
        return SimpleLocation.valueOf(str);
    }

    @Override
    protected CacheUpdateHandle<SimpleLocation, Structure> getUpdateHandle() {
        return updateHandle;
    }

    @Override
    protected CacheDeleteHandle<SimpleLocation, Structure> getDeleteHandle() {
        return deleteHandle;
    }

    @Override
    protected void onEnable() throws Exception {
        super.onEnable();

        Thread structureTikcingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(!Thread.interrupted() && base.isEnabled()) {
                    if(tick < 0)
                        tick = 0;
                    tick++;

                    try {
                        Set<SimpleLocation> slocs = getAllKeys();

                        for(SimpleLocation sloc : slocs) {
                            World world = Bukkit.getWorld(sloc.getWorld());
                            if(world == null)
                                continue;

                            SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);
                            if(!world.isChunkLoaded(scloc.getI(), scloc.getJ()))
                                continue;

                            Structure structure = get(sloc, false);
                            if(structure == null)
                                continue;

                            if(!(structure instanceof Tickable))
                                continue;

                            ((Tickable) structure).onPreTickAsync(tick);

                            try {
                                Bukkit.getScheduler().callSyncMethod(base, new Callable<Void>() {

                                    @Override
                                    public Void call() throws Exception {
                                        ((Tickable) structure).onTick(tick);

                                        return null;
                                    }

                                }).get();
                            } catch (InterruptedException e) {

                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }

        });

        structureTikcingThread.setName("Federation -- StructureTickingThread");
        structureTikcingThread.setDaemon(true);
        structureTikcingThread.start();

        Thread entityTrackingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                //clean up the reference map
                Set<UUID> deletes = new HashSet<>();
                for(Entry<UUID, WeakReference<Entity>> entry : entityTrackMap.entrySet()) {
                    if(entry.getValue().get() == null)
                        deletes.add(entry.getKey());
                }
                for(UUID delete : deletes) {
                    entityTrackMap.remove(delete);
                    entityLocationMap.remove(delete);
                }

                //track entity locations
                for(World w : Bukkit.getWorlds()) {
                    for(Entity e : w.getEntities()) {
                        UUID uuid = e.getUniqueId();

                        Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(base, new Callable<Boolean>() {

                            @Override
                            public Boolean call() throws Exception {
                                return !e.isDead() && e.isValid();
                            }

                        });

                        boolean valid = false;
                        try {
                            valid = future.get();
                        } catch (InterruptedException e1) {
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }

                        if(!valid)
                            continue;

                        if(!entityLocationMap.containsKey(uuid))
                            continue;

                        SimpleLocation previous = entityLocationMap.get(uuid);
                        SimpleLocation current = LocationUtil.convertToSimpleLocation(e.getLocation());

                        //update location if equal
                        if(!previous.equals(current)) {
                            entityLocationMap.put(uuid, current);
                            onEntityBlockMoveAsync(e, previous, current);
                        }

                    }
                }
            }

        });
        entityTrackingThread.setDaemon(true);
        entityTrackingThread.start();

        setStructureSerializer();
    }

    private void setStructureSerializer(){
        final String PROPERTY_SIMPLENAME = "SimpleName";
        final String PROPERTY_DATA = "Data";
        Database.registerTypeAdapter(Structure.class, new Serializer<Structure>() {

            @Override
            public JsonElement serialize(Structure src, Type typeOfSrc, JsonSerializationContext context) {
                Class<? extends Structure> clazz = src.getClass();
                JsonElement data = context.serialize(src, clazz);

                JsonObject obj = new JsonObject();
                obj.addProperty(PROPERTY_SIMPLENAME, clazz.getSimpleName());
                obj.add(PROPERTY_DATA, data);

                return obj;
            }

            @SuppressWarnings("unchecked")
            @Override
            public Structure deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                JsonObject obj = (JsonObject) json;

                String simpleName = obj.get(PROPERTY_SIMPLENAME).getAsString();

                Class<? extends Structure> clazz = null;
                try {
                    clazz = (Class<? extends Structure>) Class.forName(STRUCTUREPACKAGEPATH+"."+simpleName);
                }catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }

                return context.deserialize(obj.get(PROPERTY_DATA), clazz);
            }

        });
    }

    protected void addPermissionHandler(PermissionHandler handler){
        permissionHandlers.add(handler);
    }

    protected void setItemDescriptionHandler(ItemDescriptionHandler handler){
        this.itemDescriptionHandler = handler;
    }

    public void setUuidToNameParse(Structure.UUIDToNameParser uuidToNameParse) {
        this.uuidToNameParse = uuidToNameParse;
    }

    public void setNameToUUIDParse(Structure.NameToUUIDParser nameToUUIDParse) {
        this.nameToUUIDParse = nameToUUIDParse;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null)
            return;

        if(e.getHand() != EquipmentSlot.HAND)
            return;

        Structure structure = this.get(LocationUtil.convertToSimpleLocation(e.getClickedBlock().getLocation()), false);
        if(structure == null)
            return;

        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (structure instanceof ClickableLeft) {
                ((ClickableLeft) structure).onClick(base, e.getAction(), e.getPlayer(), structurePermissionFilter);
            }/* else if (e.getPlayer().isSneaking() && structure instanceof ClickableLeftShift) {
                ((ClickableLeftShift) structure).onClick(base, e.getAction(), e.getPlayer(), structurePermissionFilter);
            }*/
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (structure instanceof ClickableRight) {
                ((ClickableRight) structure).onClick(base, e.getAction(), e.getPlayer(), structurePermissionFilter);
            } else if (e.getPlayer().isSneaking() && structure instanceof ClickableRightShift) {
                ((ClickableRightShift) structure).onClick(base, e.getAction(), e.getPlayer(), structurePermissionFilter);
            }
        } else if(e.getAction() == Action.PHYSICAL) {
            if(structure instanceof ClickablePhysics) {
                ((ClickablePhysics) structure).onClick(base, e.getAction(), e.getPlayer(), structurePermissionFilter);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getBlock().getLocation());

        Structure structure = this.get(sloc, false);
        if(structure == null)
            return;

        UUID ownerUuid = structure.getOwnerUuid();
        if(ownerUuid == null && e.getPlayer().hasPermission(base.executor.adminPermission)) {
            //this shouldn't happen but allow admins to fix it manually if it happens
            this.save(sloc, null);
            return;
        }

        e.setCancelled(true);

        for(PermissionHandler handler : permissionHandlers){
            if(!handler.check(e.getPlayer(), structure)) {
                return;
            }
        }

        try {
            List<ItemStack> drops = new ArrayList<>();
            drops.addAll(e.getBlock().getDrops());

            drops.add(this.createStructureItem(e.getPlayer(), structure.getClass()));

            e.getBlock().setType(Material.AIR);
            for (ItemStack IS : drops)
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation().add(0.5, 0, 0.5), IS);
        } finally {
            //this prevents item dupe if something went wrong for item drop section.
            this.save(sloc, null);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for(Block b : e.getBlocks()) {
            if(this.get(LocationUtil.convertToSimpleLocation(b.getLocation())) != null) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for(Block b : e.getBlocks()) {
            if(this.get(LocationUtil.convertToSimpleLocation(b.getLocation())) != null) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(BlockExplodeEvent e) {
        for(ListIterator<Block> iter = e.blockList().listIterator(); iter.hasNext();) {
            Block block = iter.next();
            if(this.get(LocationUtil.convertToSimpleLocation(block.getLocation())) != null) {
                iter.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        for(ListIterator<Block> iter = e.blockList().listIterator(); iter.hasNext();) {
            Block block = iter.next();
            if(this.get(LocationUtil.convertToSimpleLocation(block.getLocation())) != null) {
                iter.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerBlockMove(PlayerBlockLocationEvent e) {
        SimpleLocation from = e.getFrom();
        SimpleLocation to = e.getTo();

        RegionTrigger fromTrigger = null;
        RegionTrigger toTrigger = null;

        if (from != null && !from.equals(to)) {
            fromTrigger = this.getRegionTriggerByCurrentLocation(from);
        }
        if (to != null && !to.equals(from)) {
            toTrigger = this.getRegionTriggerByCurrentLocation(to);
        }

        if(fromTrigger != null) {
            fromTrigger.onExit(base, e.getPlayer(), structurePermissionFilter);

            if(fromTrigger instanceof EntityTrackingRegionTrigger) {
                ((EntityTrackingRegionTrigger) fromTrigger).removeEntity(new WeakReference<Entity>(e.getPlayer()));
            }
        }

        if(toTrigger != null) {
            toTrigger.onEnter(base, e.getPlayer(), structurePermissionFilter);

            if(toTrigger instanceof EntityTrackingRegionTrigger) {
                ((EntityTrackingRegionTrigger) toTrigger).addEntity(new WeakReference<Entity>(e.getPlayer()));
            }
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getLocation());

        entityTrackMap.put(e.getEntity().getUniqueId(), new WeakReference<Entity>(e.getEntity()));
        entityLocationMap.put(e.getEntity().getUniqueId(), sloc);

        RegionTrigger trigger = this.getRegionTriggerByCurrentLocation(sloc);
        if(trigger instanceof EntityTrackingRegionTrigger) {
            ((EntityTrackingRegionTrigger) trigger).addEntity(new WeakReference<Entity>(e.getEntity()));
        }
    }

    protected void onEntityBlockMoveAsync(Entity entity, SimpleLocation from, SimpleLocation current) {
        RegionTrigger fromTrigger = this.getRegionTriggerByCurrentLocation(from);
        if(fromTrigger instanceof EntityTrackingRegionTrigger) {
            ((EntityTrackingRegionTrigger) fromTrigger).removeEntity(new WeakReference<Entity>(entity));
        }

        RegionTrigger toTrigger = this.getRegionTriggerByCurrentLocation(current);
        if(toTrigger instanceof EntityTrackingRegionTrigger) {
            ((EntityTrackingRegionTrigger) toTrigger).addEntity(new WeakReference<Entity>(entity));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getEntity().getLocation());

        entityTrackMap.remove(e.getEntity().getUniqueId());
        entityLocationMap.remove(e.getEntity().getUniqueId());

        RegionTrigger trigger = this.getRegionTriggerByCurrentLocation(sloc);
        if(trigger instanceof EntityTrackingRegionTrigger) {
            ((EntityTrackingRegionTrigger) trigger).removeEntity(new WeakReference<Entity>(e.getEntity()));
        }
    }

    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClickWithStructureItem(PlayerInteractEvent e) {
        if(e.getHand() != EquipmentSlot.HAND)
            return;

        ItemStack IS = e.getPlayer().getItemInHand();
        if(IS == null || IS.getType() == Material.AIR)
            return;

        if(!IS.getItemMeta().hasLore() || !IS.getItemMeta().getLore().contains(STRUCTURE_TAG))
            return;

        String title = IS.getItemMeta().getDisplayName();
        if(title == null || title.length() == 0)
            return;

        e.setCancelled(true);

        Block clicked = e.getClickedBlock();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(clicked.getLocation());

        //permission check
        ////////////////////////////////////////////////////////////
        Player player = e.getPlayer();

        for(PermissionHandler handler : permissionHandlers){
            if(!handler.allow(player, IS, sloc)){
                return;
            }
        }

        //structure build
        ////////////////////////////////////////////////////////////
        if(this.get(sloc) != null) {
            base.sendMessage(player, DefaultLanguages.StructureManager_AlreadyThere);
            return;
        }

        Material requiredType = Material.GOLD_BLOCK;
        if(clicked.getType() != requiredType) {
            base.lang.addString(requiredType.name());
            base.sendMessage(player, DefaultLanguages.StructureManager_NotAValidBlock);
            return;
        }

        title = ChatColor.stripColor(title);
        String className = STRUCTUREPACKAGEPATH+"."+title;
        Class<? extends Structure> structureClass = null;
        try {
            structureClass = (Class<? extends Structure>) Class.forName(className);
        }catch(ClassNotFoundException ex) {
            base.getLogger().warning("A player is trying to set up structure "+title+", but no such structure class found.");
            return;
        }

        Constructor<? extends Structure> con = null;
        try {
            con = structureClass.getConstructor(SimpleLocation.class, UUID.class);
        } catch (NoSuchMethodException ex) {
            e.getPlayer().sendMessage(
                    ChatColor.GRAY + title + " does not have appropriate constructor. This is developer's fault.");
            return;
        } catch (SecurityException ex) {
            ex.printStackTrace();
            return;
        }

        if(InventoryUtil.consumeOneItemInHand(e.getPlayer())) {
            try {
                Structure structure = con.newInstance(sloc, player.getUniqueId());
                setStructure(sloc, structure);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e1) {
                e.getPlayer().sendMessage(ChatColor.RED+"Something went wrong");
                base.getLogger().severe(player.getName()+" spent a structure item for "+title+" but structure couldn't be set");
            }
        }else {
            e.getPlayer().sendMessage(ChatColor.RED+"Failed to consume item");
        }
    }

    public ItemStack createStructureItem(Player player, Class<? extends Structure> clazz) {
        String title = ChatColor.DARK_AQUA+clazz.getSimpleName();
        List<String> lore = getDescriptionForStructureItem(player, clazz);
        lore.add("");
        lore.add(STRUCTURE_TAG);

        ItemStack IS = new ItemStack(Material.GOLD_RECORD);
        ItemMeta IM = IS.getItemMeta();
        IM.setDisplayName(title);
        IM.setLore(lore);
        IS.setItemMeta(IM);

        return IS;
    }

    private static final String DESC_FORMAT = "Structure_%s_Description";
    protected List<String> getDescriptionForStructureItem(Player player, Class<? extends Structure> clazz) {
        return itemDescriptionHandler == null ?  null : itemDescriptionHandler.getDescriptionFor(player, clazz);
    }

    public void setStructure(SimpleLocation sloc, Structure structure) {
        this.save(sloc, structure);
    }

    public Structure getStructure(SimpleLocation sloc) {
        return this.get(sloc);
    }

    /**
     * Attempt to edit structure information sitting at provided location
     * @param player editor
     * @param sloc target location
     * @return true if edit started; false if no structure found at that location.
     */
    public boolean startStructureEdit(Player player, SimpleLocation sloc) {
        Structure structure = this.get(sloc);
        if(structure == null)
            return false;

        PropertyEditManager pemanager = base.getManager(PropertyEditManager.class);
        pemanager.startEdit(player,
                sloc.toString(),
                structure.infoToMap(player, uuidToNameParse),
                new ConversationAbandonedListener() {

            @Override
            public void conversationAbandoned(ConversationAbandonedEvent arg0) {
                Map<Language, Object> props = (Map<Language, Object>) arg0.getContext().getSessionData(PropertyEditManager.PROPERTY_SESSIONDATANAME);

                try {
                    structure.applyInfoMap(player, props, nameToUUIDParse);

                    save(sloc, structure);
                }catch(Structure.StructureException e) {
                    Language lang = e.getLang();
                    if(lang != null)
                        base.sendMessage(player, lang);
                    else
                        player.sendMessage("Undefined Language for Exception");
                }
            }

        });

        return true;
    }

    public List<Structure> getAllStructures() {
        return getAllStructures(null);
    }

    public List<Structure> getAllStructures(StructureFilter filter) {
        List<Structure> structures = new ArrayList<>();
        for(SimpleLocation key : this.getAllKeys()) {
            Structure structure = this.get(key);
            if(structure == null)
                continue;

            if(filter != null && !filter.allow(structure.getClass()))
                continue;

            structures.add(structure);
        }

        return structures;
    }

    private void addNewTriggerToChunks(SimpleLocation key, RegionTrigger value) {
        Area area = value.getArea();

        for(SimpleChunkLocation scloc : Area.getAllChunkLocations(area)) {
            Set<SimpleLocation> structures = structuresInChunk.get(scloc);
            if(structures == null) {
                structures = new HashSet<SimpleLocation>();
                structuresInChunk.put(scloc, structures);
            }

            structures.add(key);
        }
    }

    private void removeTriggerFromAllChunks(SimpleLocation key, RegionTrigger value) {
        Area area = value.getArea();

        for(SimpleChunkLocation scloc : Area.getAllChunkLocations(area)) {
            Set<SimpleLocation> structures = structuresInChunk.get(scloc);
            if(structures == null) {
                return;
            }

            structures.remove(key);

            if(structures.isEmpty())
                structuresInChunk.remove(scloc);
        }
    }

    private RegionTrigger getRegionTriggerByCurrentLocation(SimpleLocation current) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(current);

        Set<SimpleLocation> slocs = structuresInChunk.get(scloc);
        if(slocs == null)
            return null;

        for(SimpleLocation sloc : slocs) {
            Structure structure = get(sloc, false);
            if(structure != null && structure instanceof RegionTrigger) {
                RegionTrigger trigger = (RegionTrigger) structure;
                if(trigger.getArea().isContainingLocation(current))
                    return trigger;
            }
        }

        return null;
    }

    private final CacheUpdateHandle<SimpleLocation, Structure> updateHandle = new CacheUpdateHandle<SimpleLocation, Structure>(){

        @Override
        public Structure onUpdate(SimpleLocation key, Structure original) {
            if(original instanceof RegionTrigger) {
                addNewTriggerToChunks(key, (RegionTrigger) original);
            }

            return null;
        }

    };

    private final CacheDeleteHandle<SimpleLocation, Structure> deleteHandle = new CacheDeleteHandle<SimpleLocation, Structure>() {

        @Override
        public void onDelete(SimpleLocation key, Structure deleted) {
            if(deleted instanceof RegionTrigger) {
                removeTriggerFromAllChunks(key, (RegionTrigger) deleted);
            }
        }

    };

    private final EntityFilter<Player> structurePermissionFilter = new EntityFilter<Player>() {

        @Override
        public boolean isPermitted(Structure structure, Player entity) {
            if(structure.getOwnerUuid() == null)
                return false;

            for(PermissionHandler handler : permissionHandlers){
                if(!handler.check(entity, structure))
                    return false;
            }

            return true;
        }

    };

    public interface StructureFilter{
        boolean allow(Class<? extends Structure> type);
    }

    public interface PermissionHandler{
        /**
         * Check if this player is allowed to use the structure.
         * @param player
         * @param structure
         * @return
         */
        boolean check(Player player, Structure structure);

        /**
         * Check if this player is allowed to set a structure at the location.
         * @param player
         * @param item the item used to set the structure.
         * @param location
         * @return
         */
        boolean allow(Player player, ItemStack item, SimpleLocation location);
    }

    public interface ItemDescriptionHandler{
        List<String> getDescriptionFor(Player player, Class<? extends Structure> clazz);
    }
}
