package org.tmarchuk.wurmunlimited.client.tooltips;

/**
 * Created by Tyson Marchuk on 2016-04-02.
 */

// From Wurm Unlimited Client
import com.wurmonline.client.GameCrashedException;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.PickableUnit;

// From Ago's modloader
import org.gotti.wurmunlimited.modcomm.Channel;
import org.gotti.wurmunlimited.modcomm.IChannelListener;
import org.gotti.wurmunlimited.modcomm.ModComm;
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

    private static final byte TYPE_STRUCTURE_QUALITY = 1;
    private static final byte TYPE_STRUCTURE_DAMAGE = 2;
    private static final byte TYPE_ITEM_QUALITY = 3;
    private static final byte TYPE_ITEM_DAMAGE = 4;

    private static Channel channel;

    private World theWorld_ = null;
    private static final byte[] stringByteArray = new byte['\uffff'];

    @Override
    public void init()
    {
        try
        {
            // Handle server communications to send structure quality and damage via mod channel
            channel = ModComm.registerChannel("tmarchuk.tooltips", new IChannelListener() {
                @Override
                public void handleMessage(ByteBuffer message) {
                    byte type = message.get();
                    long id = message.getLong();
                    float value = message.getFloat();
                    switch (type) {
                        case TYPE_STRUCTURE_QUALITY:
                            structureQuality_.put(id, value);
                            break;
                        case TYPE_STRUCTURE_DAMAGE:
                            structureDamage_.put(id, value);
                            break;
                        case TYPE_ITEM_QUALITY:
                            itemQuality_.put(id, value);
                            break;
                        case TYPE_ITEM_DAMAGE:
                            itemDamage_.put(id, value);
                            break;
                        default:
                            logger_.warning("Unknown data type from server: " + type);
                    }
                }
            });

            ClassPool classPool = HookManager.getInstance().getClassPool();
            String descriptor;

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

