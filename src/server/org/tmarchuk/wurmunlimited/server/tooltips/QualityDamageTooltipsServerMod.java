package org.tmarchuk.wurmunlimited.server.tooltips;

/**
 * Created by Tyson Marchuk on 2016-04-02.
 */

// From Wurm Unlimited Dedicated Server
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.DbItem;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.*;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

// From Ago's modloader
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;

// Javassist
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.Descriptor;
import javassist.NotFoundException;

// Base Java
import java.util.ArrayList;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QualityDamageTooltipsServerMod implements WurmMod, Initable
{
    private static final Logger logger_ = Logger.getLogger(QualityDamageTooltipsServerMod.class.getName());

    public static void logException(String msg, Throwable e)
    {
        if (logger_ != null)
            logger_.log(Level.SEVERE, msg, e);
    }

    private static final String STRUCTURE_QUALITY_WINDOW_TITLE = ":mod:structure_quality_info";
    private static final String STRUCTURE_DAMAGE_WINDOW_TITLE = ":mod:structure_damage_info";
    private static final String ITEM_QUALITY_WINDOW_TITLE = ":mod:item_quality_info";
    private static final String ITEM_DAMAGE_WINDOW_TITLE = ":mod:item_damage_info";

    @Override
    public void init()
    {
        try
        {
            // Send structure quality and damage via chat message to hidden chat tab when added.
            // =================================================================================

            // com.wurmonline.server.creatures.Communicator.sendAddWall(long structureId, Wall wall)
            ClassPool classPool = HookManager.getInstance().getClassPool();
            String descriptor;
            descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { CtClass.longType,
                    classPool.get("com.wurmonline.server.structures.Wall") });

            HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Communicator", "sendAddWall", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Send the wall as per normal.
                                        Object result = method.invoke(proxy, args);

                                        // Get the communicator/wall objects.
                                        Communicator comm = (Communicator) proxy;
                                        Wall theWall = (Wall) args[1];
                                        long id = theWall.getId();

                                        // Send a chat message with the quality/damage.
                                        sendStructureQualityAndDamage(comm, id, theWall.getQualityLevel(), theWall.getDamage());

                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.creatures.Communicator.sendAddWall(long structureId, Wall wall)

            // com.wurmonline.server.creatures.Communicator.sendAddFence(Fence fence)
            descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { classPool.get("com.wurmonline.server.structures.Fence") });

            HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Communicator", "sendAddFence", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Send the fence as per normal.
                                        Object result = method.invoke(proxy, args);

                                        // Get the communicator/fence objects.
                                        Communicator comm = (Communicator) proxy;
                                        Fence theFence = (Fence) args[0];
                                        long id = theFence.getId();

                                        // Send a chat message with the quality/damage.
                                        sendStructureQualityAndDamage(comm, id, theFence.getQualityLevel(), theFence.getDamage());

                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.creatures.Communicator.sendAddFence(Fence fence)

            // com.wurmonline.server.creatures.Communicator.sendAddFloor(long structureId, Floor floor)
            descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { CtClass.longType,
                    classPool.get("com.wurmonline.server.structures.Floor") });

            HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Communicator", "sendAddFloor", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Send the floor as per normal.
                                        Object result = method.invoke(proxy, args);

                                        // Get the communicator/floor objects.
                                        Communicator comm = (Communicator) proxy;
                                        Floor theFloor = (Floor) args[1];
                                        long id = theFloor.getId();

                                        // Send a chat message with the quality/damage.
                                        sendStructureQualityAndDamage(comm, id, theFloor.getQualityLevel(), theFloor.getDamage());

                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.creatures.Communicator.sendAddFloor(long structureId, Floor floor)

            // com.wurmonline.server.creatures.Communicator.sendAddBridgePart(long structureId, BridgePart bridgePart)
            descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { CtClass.longType,
                    classPool.get("com.wurmonline.server.structures.BridgePart") });

            HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Communicator", "sendAddBridgePart", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Send the BridgePart as per normal.
                                        Object result = method.invoke(proxy, args);

                                        // Get the communicator/BridgePart objects.
                                        Communicator comm = (Communicator) proxy;
                                        BridgePart theBridgePart = (BridgePart) args[1];
                                        long id = theBridgePart.getId();

                                        // Send a chat message with the quality/damage.
                                        sendStructureQualityAndDamage(comm, id, theBridgePart.getQualityLevel(), theBridgePart.getDamage());

                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.creatures.Communicator.sendAddBridgePart(long structureId, BridgePart bridgePart)

            // Handle quality changing
            // =======================
            // com.wurmonline.server.structures.DbBridgePart.setQualityLevel(float ql):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType });

            HookManager.getInstance().registerHook("com.wurmonline.server.structures.DbBridgePart", "setQualityLevel", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbBridgePart bridgePart = (DbBridgePart) proxy;
                                        long id = bridgePart.getId();
                                        float originalQuality = bridgePart.getQualityLevel();

                                        // Set the quality as per normal and if it changes the quality then notify all the
                                        // tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newQuality = bridgePart.getQualityLevel();
                                        if (newQuality != originalQuality)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(bridgePart.getTile());
                                            for (Communicator comm : comms)
                                            {
                                                sendStructureQuality(comm, id, newQuality);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.structures.DbBridgePart.setQualityLevel(float ql):boolean

            // com.wurmonline.server.structures.DbFence.setQualityLevel(float ql):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType });

            HookManager.getInstance().registerHook("com.wurmonline.server.structures.DbFence", "setQualityLevel", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbFence fence = (DbFence) proxy;
                                        long id = fence.getId();
                                        float originalQuality = fence.getQualityLevel();

                                        // Set the quality as per normal and if it changes the quality then notify all the
                                        // tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newQuality = fence.getQualityLevel();
                                        if (newQuality != originalQuality)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(fence.getTile());
                                            for (Communicator comm : comms)
                                            {
                                                sendStructureQuality(comm, id, newQuality);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.structures.DbFence.setQualityLevel(float ql):boolean

            // com.wurmonline.server.structures.DbFloor.setQualityLevel(float ql):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType });

            HookManager.getInstance().registerHook("com.wurmonline.server.structures.DbFloor", "setQualityLevel", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbFloor floor = (DbFloor) proxy;
                                        long id = floor.getId();
                                        float originalQuality = floor.getQualityLevel();

                                        // Set the quality as per normal and if it changes the quality then notify all the
                                        // tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newQuality = floor.getQualityLevel();
                                        if (newQuality != originalQuality)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(floor.getTile());
                                            for (Communicator comm : comms)
                                            {
                                                sendStructureQuality(comm, id, newQuality);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.structures.DbFloor.setQualityLevel(float ql):boolean

            // com.wurmonline.server.structures.DbWall.setQualityLevel(float ql):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType });

            HookManager.getInstance().registerHook("com.wurmonline.server.structures.DbWall", "setQualityLevel", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbWall wall = (DbWall) proxy;
                                        long id = wall.getId();
                                        float originalQuality = wall.getQualityLevel();

                                        // Set the quality as per normal and if it changes the quality then notify all the
                                        // tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newQuality = wall.getQualityLevel();
                                        if (newQuality != originalQuality)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(wall.getTile());
                                            for (Communicator comm : comms)
                                            {
                                                sendStructureQuality(comm, id, newQuality);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.structures.DbWall.setQualityLevel(float ql):boolean

            // Handle damage changing
            // com.wurmonline.server.structures.DbBridgePart.setDamage(float aDamage):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType });

            HookManager.getInstance().registerHook("com.wurmonline.server.structures.DbBridgePart", "setDamage", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbBridgePart bridgePart = (DbBridgePart) proxy;
                                        long id = bridgePart.getId();
                                        float originalDamage = bridgePart.getDamage();

                                        // Set the damage as per normal and if it changes the damage then notify all the
                                        // tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newDamage = bridgePart.getDamage();
                                        if (newDamage != originalDamage)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(bridgePart.getTile());
                                            for (Communicator comm : comms)
                                            {
                                                sendStructureDamage(comm, id, newDamage);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.structures.DbBridgePart.setDamage(float aDamage):boolean

            // com.wurmonline.server.structures.DbFence.setDamage(float aDamage):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType });

            HookManager.getInstance().registerHook("com.wurmonline.server.structures.DbFence", "setDamage", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbFence fence = (DbFence) proxy;
                                        long id = fence.getId();
                                        float originalDamage = fence.getDamage();

                                        // Set the damage as per normal and if it changes the damage then notify all the
                                        // tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newDamage = fence.getDamage();
                                        if (newDamage != originalDamage)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(fence.getTile());
                                            for (Communicator comm : comms)
                                            {
                                                sendStructureDamage(comm, id, newDamage);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.structures.DbFence.setDamage(float aDamage):boolean

            // com.wurmonline.server.structures.DbFloor.setDamage(float aDamage):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType });

            HookManager.getInstance().registerHook("com.wurmonline.server.structures.DbFloor", "setDamage", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbFloor floor = (DbFloor) proxy;
                                        long id = floor.getId();
                                        float originalDamage = floor.getDamage();

                                        // Set the damage as per normal and if it changes the damage then notify all the
                                        // tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newDamage = floor.getDamage();
                                        if (newDamage != originalDamage)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(floor.getTile());
                                            for (Communicator comm : comms)
                                            {
                                                sendStructureDamage(comm, id, newDamage);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.structures.DbFloor.setDamage(float aDamage):boolean

            // com.wurmonline.server.structures.DbWall.setDamage(float aDamage):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType });

            HookManager.getInstance().registerHook("com.wurmonline.server.structures.DbWall", "setDamage", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbWall wall = (DbWall) proxy;
                                        long id = wall.getId();
                                        float originalDamage = wall.getDamage();

                                        // Set the damage as per normal and if it changes the damage then notify all the
                                        // tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newDamage = wall.getDamage();
                                        if (newDamage != originalDamage)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(wall.getTile());
                                            for (Communicator comm : comms)
                                            {
                                                sendStructureDamage(comm, id, newDamage);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.structures.DbWall.setDamage(float aDamage):boolean

            // Items (on ground)
            // -----------------

            // Handle forge, BSB, guard tower, etc. These are items on the ground. The initial values are sent with
            // sendAddItem but updates aren't sent so we modify the DbItem code that updates quality/damage
            // and send this in a third/fourth chat channel.

            // com.wurmonline.server.items.DbItem.setQualityLevel(float qlevel):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType });

            HookManager.getInstance().registerHook("com.wurmonline.server.items.DbItem", "setQualityLevel", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbItem item = (DbItem) proxy;
                                        long id = item.getWurmId();
                                        float originalQuality= item.getQualityLevel();

                                        // Set the quality as per normal and if it changes the quality then notify all
                                        // the tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newQuality = item.getQualityLevel();
                                        if (newQuality != originalQuality)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(getTileIfGroundItem(item));
                                            for (Communicator comm : comms)
                                            {
                                                sendItemQuality(comm, id, newQuality);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.items.DbItem.setQualityLevel(float qlevel):boolean

            // com.wurmonline.server.items.DbItem.setDamage(float dam, boolean overrideIndestructible):boolean
            descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { CtClass.floatType, CtClass.booleanType });

            HookManager.getInstance().registerHook("com.wurmonline.server.items.DbItem", "setDamage", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        // Convert to real type and get the starting values.
                                        DbItem item = (DbItem) proxy;
                                        long id = item.getWurmId();
                                        float originalDamage= item.getDamage();

                                        // Set the quality as per normal and if it changes the quality then notify all
                                        // the tile watchers.
                                        Object result = method.invoke(proxy, args);
                                        float newDamage = item.getDamage();
                                        if (newDamage != originalDamage)
                                        {
                                            // Get a list of watchers.
                                            Communicator[] comms = getValidRecipients(getTileIfGroundItem(item));
                                            for (Communicator comm : comms)
                                            {
                                                sendItemDamage(comm, id, newDamage);
                                            }
                                        }
                                        return result;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.items.DbItem.setDamage(float dam, boolean overrideIndestructible):boolean

            // Handle 'vehicles' which include boats, rafts, carts, etc.
            // ---------------------------------------------------------

            // com.wurmonline.server.creatures.Communicator.sendNewMovingItem(long id, String name, String model,
            //      float x, float y, float z, long onBridge, float rot, byte layer, boolean onGround, boolean floating,
            //      boolean isSolid, byte material, byte rarity)
            descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] { CtClass.longType, classPool.get("java.lang.String"),
                    classPool.get("java.lang.String"), CtClass.floatType, CtClass.floatType, CtClass.floatType, CtClass.longType,
                    CtClass.floatType, CtClass.byteType, CtClass.booleanType, CtClass.booleanType, CtClass.booleanType,
                    CtClass.byteType, CtClass.byteType});

            HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Communicator", "sendNewMovingItem", descriptor,
                    new InvocationHandlerFactory()
                    {
                        @Override
                        public InvocationHandler createInvocationHandler()
                        {
                            return new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    synchronized (proxy)
                                    {
                                        Communicator comm = (Communicator) proxy;
                                        long id = (long) args[0];
                                        String name = args[1].toString();
                                        if (Items.exists(id))
                                        {
                                            // It's a vehicle. Find out the quality and damage and send. We use the
                                            // 'item' channels because this is a WurmId so it won't collide with other
                                            // item Wurm IDs. It's also an item even though it's a creature...
                                            Item theItem = Items.getItem(id);
                                            sendItemQuality(comm, id, theItem.getQualityLevel());
                                            sendItemDamage(comm, id, theItem.getDamage());
                                        }
                                    }

                                    // Call the original.
                                    return method.invoke(proxy, args);
                                }
                            };
                        }
                    });
            // END - com.wurmonline.server.zones.VirtualZone.addCreature(long creatureId, boolean overRideRange):boolean
        }
        catch (NotFoundException e)
        {
            logException("Failed to create hooks for " + QualityDamageTooltipsServerMod.class.getName(), e);
            throw new HookException(e);
        }
    }

    // Send quality and damage of a structure to client in hidden chat tab.
    private void sendStructureQualityAndDamage(Communicator comm, long id, float quality, float damage)
    {
        sendData(comm, id, quality, STRUCTURE_QUALITY_WINDOW_TITLE);
        sendData(comm, id, damage, STRUCTURE_DAMAGE_WINDOW_TITLE);
    }

    // Send quality of a structure to client in hidden chat tab.
    private void sendStructureQuality(Communicator comm, long id, float quality)
    {
        sendData(comm, id, quality, STRUCTURE_QUALITY_WINDOW_TITLE);
    }

    // Send damage of a structure to client in hidden chat tab.
    private void sendStructureDamage(Communicator comm, long id, float damage)
    {
        sendData(comm, id, damage, STRUCTURE_DAMAGE_WINDOW_TITLE);
    }

    // Send quality of an item to client in hidden chat tab.
    private void sendItemQuality(Communicator comm, long id, float quality)
    {
        sendData(comm, id, quality, ITEM_QUALITY_WINDOW_TITLE);
    }

    // Send damage of an item to client in hidden chat tab.
    private void sendItemDamage(Communicator comm, long id, float damage)
    {
        sendData(comm, id, damage, ITEM_DAMAGE_WINDOW_TITLE);
    }

    // Send data about an item or structure to a private chat channel.
    private void sendData(Communicator comm, long id, float value, String window)
    {
        Message theMsg = new Message(comm.getPlayer(), (byte) 10, window, id + ":" + value);
        comm.sendMessage(theMsg);
    }

    private Communicator[] getValidRecipients(VolaTile tile)
    {
        ArrayList<Communicator> result = new ArrayList<>();
        if (tile != null)
        {
            VirtualZone[] watchers = tile.getWatchers();
            for (VirtualZone curWatcher : watchers)
            {
                // For each watcher that is a player, get the communicator.
                if (curWatcher.getWatcher().isPlayer() && curWatcher.getWatcher().hasLink())
                {
                    result.add(curWatcher.getWatcher().getCommunicator());
                }
            }
        }
        return (Communicator[]) result.toArray(new Communicator[result.size()]);
    }

    private VolaTile getTileIfGroundItem(DbItem item)
    {
        VolaTile result = null;

        // Ground tiles have no parent but do have a valid zoneId.
        if((item.zoneId != -10L) && (item.getParentId() == -10L))
        {
            result = Zones.getTileOrNull(item.getTileX(), item.getTileY(), item.isOnSurface());
        }
        return result;
    }
}
