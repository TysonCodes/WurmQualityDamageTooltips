package org.tmarchuk.wurmunlimited.client.tooltips;

/**
 * Created by Tyson Marchuk on 2016-04-02.
 */

// From Wurm Unlimited Client
import com.wurmonline.client.GameCrashedException;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.PickableUnit;

// From Ago's modloader
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;
import org.gotti.wurmunlimited.modsupport.ModClient;

// Javassist
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.Descriptor;
import javassist.NotFoundException;

// Base Java
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QualityDamageTooltipsClientMod implements WurmMod, Initable
{
    private static final Logger logger_ = Logger.getLogger(QualityDamageTooltipsClientMod.class.getName());

    public static void logException(String msg, Throwable e)
    {
        if (logger_ != null)
            logger_.log(Level.SEVERE, msg, e);
    }

    private HashMap<Long, Float> structureQuality_ = new HashMap<>();
    private HashMap<Long, Float> structureDamage_ = new HashMap<>();
    private HashMap<Long, Float> itemQuality_ = new HashMap<>();
    private HashMap<Long, Float> itemDamage_ = new HashMap<>();
    private HashMap<Long, Float> invalidHash_ = new HashMap<>();

    private static final String STRUCTURE_QUALITY_WINDOW_TITLE = ":mod:structure_quality_info";
    private static final String STRUCTURE_DAMAGE_WINDOW_TITLE = ":mod:structure_damage_info";
    private static final String ITEM_QUALITY_WINDOW_TITLE = ":mod:item_quality_info";
    private static final String ITEM_DAMAGE_WINDOW_TITLE = ":mod:item_damage_info";

    private World theWorld_ = null;
    private static final byte[] stringByteArray = new byte['\uffff'];

    @Override
    public void init()
    {
        try
        {
            // Handle added server communications to send structure quality and damage via chat message to hidden
            // chat tab.

            // com.wurmonline.client.comm.ServerConnectionListenerClass.textMessage(String title, float r, float g, float b, String message, byte onScreenType)
            ClassPool classPool = HookManager.getInstance().getClassPool();
            String descriptor;
            descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] {classPool.get("java.lang.String"),
                    CtClass.floatType, CtClass.floatType, CtClass.floatType, classPool.get("java.lang.String"),
                    CtClass.byteType});

            HookManager.getInstance().registerHook(
                    "com.wurmonline.client.comm.ServerConnectionListenerClass", "textMessage", descriptor,
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
                                        String windowTitle = args[0].toString();
                                        String message = args[4].toString();
                                        boolean messageForMod = false;
                                        HashMap<Long, Float> hashToSet = invalidHash_;
                                        if (windowTitle.equals(STRUCTURE_QUALITY_WINDOW_TITLE))
                                        {
                                            hashToSet = structureQuality_;
                                            messageForMod = true;
                                        }
                                        else if (windowTitle.equals(STRUCTURE_DAMAGE_WINDOW_TITLE))
                                        {
                                            hashToSet = structureDamage_;
                                            messageForMod = true;
                                        }
                                        else if (windowTitle.equals(ITEM_QUALITY_WINDOW_TITLE))
                                        {
                                            hashToSet = itemQuality_;
                                            messageForMod = true;
                                        }
                                        else if (windowTitle.equals(ITEM_DAMAGE_WINDOW_TITLE))
                                        {
                                            hashToSet = itemDamage_;
                                            messageForMod = true;
                                        }

                                        if(messageForMod)
                                        {
                                            // Add tooltip info to the hashes.
                                            String[] parameters = message.split(":", 2);
                                            if (parameters.length == 2)
                                            {
                                                Long id = Long.valueOf(parameters[0]);
                                                Float value = Float.valueOf(parameters[1]);
                                                hashToSet.put(id, value);
                                            }
                                            return null;
                                        }
                                        else
                                        {
                                            return method.invoke(proxy, args);
                                        }
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.client.comm.ServerConnectionListenerClass.textMessage(String title, float r, float g, float b, String message, byte onScreenType)

            // Handle items being added. The quality is sent but for some reason ignored.
            // com.wurmonline.client.comm.SimpleServerConnectionClass.receiveItemOrCorpse(long creatureDeadId, ByteBuffer bb)
            descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] {CtClass.longType, classPool.get("java.nio.ByteBuffer")});

            HookManager.getInstance().registerHook(
                    "com.wurmonline.client.comm.SimpleServerConnectionClass", "receiveItemOrCorpse", descriptor,
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
                                        // Make a copy of the buffer.
                                        long creatureId = (long) args[0];
                                        ByteBuffer bufOrig = (ByteBuffer) args[1];
                                        ByteBuffer bufCopy = bufOrig.duplicate();

                                        // Call the original.
                                        method.invoke(proxy, args);

                                        // Parse it ourselves to get out the quality and damage info if it's not a corpse.
                                        if (creatureId < 0L)
                                        {
                                            long itemId = bufCopy.getLong();
                                            bufCopy.getFloat(); // x
                                            bufCopy.getFloat(); // y
                                            bufCopy.getFloat(); // rot
                                            bufCopy.getFloat(); // h
                                            String lName = readStringByteLength(bufCopy);
                                            readStringByteLength(bufCopy); // modelName
                                            bufCopy.get(); // layer
                                            bufCopy.get(); // materialId
                                            readStringByteLength(bufCopy); // lDescription
                                            bufCopy.getShort(); // iconId
                                            if(bufCopy.get() == 1) {
                                                float quality = bufCopy.getFloat();
                                                float damage = bufCopy.getFloat();
                                                // Store these in the hashes.
                                                itemQuality_.put(itemId, quality);
                                                itemDamage_.put(itemId, damage);
                                                logger_.log(Level.FINER, "Added an item. Name:'" + lName + "', Item ID:" + itemId + ", quality:" + quality + ", damage:" + damage);
                                            }
                                        }

                                        return null;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.client.comm.SimpleServerConnectionClass.receiveItemOrCorpse(long creatureDeadId, ByteBuffer bb)

            // TODO: Cleanup hash table for items no longer visible.
            // TODO: com.wurmonline.client.comm.ServerConnectionListenerClass.removeWall(long houseId, int x, int y, int heightOffset, byte dir, byte layer)
            // TODO: com.wurmonline.client.comm.ServerConnectionListenerClass.removeRoof(long houseId, int x, int y, short heightOffset, byte layer)
            // TODO: com.wurmonline.client.comm.ServerConnectionListenerClass.removeFloor(long houseId, int x, int y, short heightOffset, byte layer)
            // TODO: com.wurmonline.client.comm.ServerConnectionListenerClass.removeBridgePart(long structureId, int x, int y, short heightOffset)
            // TODO: com.wurmonline.client.comm.ServerConnectionListenerClass.removeFence(int x, int y, int heightOffset, byte dir, byte layer)
            // TODO: com.wurmonline.client.comm.ServerConnectionListenerClass.deleteCreature(long id)
            // TODO: com.wurmonline.client.comm.ServerConnectionListenerClass.removeItem(long id)

            // Add quality and damage information to hover text.
            // com.wurmonline.client.renderer.PickData.addText(String line)
            descriptor = Descriptor.ofMethod(CtClass.voidType, new CtClass[] {classPool.get("java.lang.String")});

            HookManager.getInstance().registerHook("com.wurmonline.client.renderer.PickData", "addText", descriptor,
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
                                    if (theWorld_ == null)
                                    {
                                        theWorld_ = ModClient.getWorld();
                                    }
                                    synchronized (proxy)
                                    {
                                        // Let it do the normal thing first.
                                        method.invoke(proxy, args);

                                        // Now get the additional text for the picked object.
                                        if (theWorld_ != null)
                                        {
                                            PickableUnit pickTarget = theWorld_.getCurrentHoveredObject();
                                            if (pickTarget != null)
                                            {
                                                String extraText = generateQualityDamageString(pickTarget.getId());
                                                if (!extraText.equals(""))
                                                {
                                                    Object[] newArgs = {extraText};
                                                    method.invoke(proxy, newArgs);
                                                }
                                            }
                                        }

                                        return null;
                                    }
                                }
                            };
                        }
                    });
            // END - com.wurmonline.client.comm.ServerConnectionListenerClass.textMessage(String title, float r, float g, float b, String message, byte onScreenType)
        }
        catch (NotFoundException e)
        {
            logException("Failed to create hooks for "  + QualityDamageTooltipsClientMod.class.getName(), e);
            throw new HookException(e);
        }
    }

    private String generateQualityDamageString(Long id)
    {
        String result = "";
        if (structureQuality_.containsKey(id))
        {
            if (structureDamage_.containsKey(id) && (structureDamage_.get(id) != 0.0f))
            {
                result = " (QL=" + structureQuality_.get(id).toString() + ", Dam=" +
                        structureDamage_.get(id).toString() + ")";
            }
            else
            {
                result = " (QL=" + structureQuality_.get(id).toString() + ")";
            }
        }
        else if (structureDamage_.containsKey(id) && (structureDamage_.get(id) != 0.0f))
        {
            result = " (Dam=" + structureDamage_.get(id).toString() + ")";
        }
        else if (itemQuality_.containsKey(id))
        {
            if ((itemDamage_.containsKey(id)) && (itemDamage_.get(id) != 0.0f))
            {
                result = " (QL=" + itemQuality_.get(id).toString() + ", Dam=" +
                        itemDamage_.get(id).toString() + ")";
            }
            else
            {
                result = " (QL=" + itemQuality_.get(id).toString() + ")";
            }
        }
        else if ((itemDamage_.containsKey(id)) && (itemDamage_.get(id) != 0.0f))
        {
            result = " (Dam=" + itemDamage_.get(id).toString() + ")";
        }

        return result;
    }

    private String readStringGivenLength(ByteBuffer bb, int length) {
        if(length > '\uffff') {
            throw GameCrashedException.forFailure("String too long!");
        } else {
            bb.get(stringByteArray, 0, length);

            try {
                return new String(stringByteArray, 0, length, "UTF-8");
            } catch (UnsupportedEncodingException var4) {
                throw GameCrashedException.forFailure("Impossible encoding error, alert devs.", var4);
            }
        }
    }

    private String readStringByteLength(ByteBuffer bb) {
        return this.readStringGivenLength(bb, bb.get() & 255);
    }
}

