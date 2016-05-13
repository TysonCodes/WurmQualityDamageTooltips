package org.tmarchuk.wurmunlimited.server.tooltips;

/**
 * Created by Tyson Marchuk on 2016-04-24.
 */

// From Wurm Unlimited Dedicated Server
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.DbItem;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import org.gotti.wurmunlimited.modcomm.Channel;
import org.gotti.wurmunlimited.modcomm.IChannelListener;
import org.gotti.wurmunlimited.modcomm.ModComm;

// Base Java
import java.nio.ByteBuffer;
import java.util.*;

public class QualityDamageTooltipsServerHelper
{
    private static final byte TYPE_STRUCTURE_QUALITY = 1;
    private static final byte TYPE_STRUCTURE_DAMAGE = 2;
    private static final byte TYPE_ITEM_QUALITY = 3;
    private static final byte TYPE_ITEM_DAMAGE = 4;

    static Channel channel;
    static Map<Player, List<ByteBuffer>> sendQueue = new HashMap<>();

    static void setupChannel() {
        channel = ModComm.registerChannel("tmarchuk.tooltips", new IChannelListener() {
            @Override
            public void onPlayerConnected(Player player) {
                // Send all queued messages to a player once the channel is activated
                if (sendQueue.containsKey(player)) {
                    List<ByteBuffer> queue = sendQueue.remove(player);
                    for (ByteBuffer msg: queue)
                        channel.sendMessage(player, msg);
                }
            }
        });
    }

    static public void cleanQueue() {
        if (sendQueue.size()>0) {
            for (Player player: sendQueue.keySet()) {
                if (player.getLastLogin() < System.currentTimeMillis() - 10000 && !channel.isActiveForPlayer(player)) {
                    // Player logged in more than 10 seconds ago and channel is still not active - they probably don't
                    // have the client-side mod. Remove them from the queue.
                    sendQueue.remove(player);
                }
            }
        }
    }

    // Send quality and damage of a structure to client in hidden chat tab.
    public static void sendStructureQualityAndDamage(Communicator comm, long id, float quality, float damage)
    {
        sendData(comm, id, quality, TYPE_STRUCTURE_QUALITY);
        sendData(comm, id, damage, TYPE_STRUCTURE_DAMAGE);
    }

    // Send quality of a structure to client in hidden chat tab.
    public static void sendStructureQuality(Communicator comm, long id, float quality)
    {
        sendData(comm, id, quality, TYPE_STRUCTURE_QUALITY);
    }

    // Send damage of a structure to client in hidden chat tab.
    public static void sendStructureDamage(Communicator comm, long id, float damage)
    {
        sendData(comm, id, damage, TYPE_STRUCTURE_DAMAGE);
    }

    // Send quality of an item to client in hidden chat tab.
    public static void sendItemQuality(Communicator comm, long id, float quality)
    {
        sendData(comm, id, quality, TYPE_ITEM_QUALITY);
    }

    // Send damage of an item to client in hidden chat tab.
    public static void sendItemDamage(Communicator comm, long id, float damage)
    {
        sendData(comm, id, damage, TYPE_ITEM_DAMAGE);
    }

    // Send data about an item or structure to a private chat channel.
    public static void sendData(Communicator comm, long id, float value, byte type)
    {
        if (comm.player == null) return; // Not a player or something weird
        if (channel.isActiveForPlayer(comm.player) || comm.player.getLastLogin() > System.currentTimeMillis() - 10000) {
            // Only runs if player channel is active, or player logged in in the last 10 seconds
            // Each packet is 13 bytes, 1 for type, 8 for id and 4 for the value
            ByteBuffer bb = ByteBuffer.allocate(13);
            bb.put(type);
            bb.putLong(id);
            bb.putFloat(value);
            bb.flip();

            if (channel.isActiveForPlayer(comm.player)) {
                // Channel is active - send right away
                channel.sendMessage(comm.player, bb);
            } else if (comm.player.getLastLogin() > System.currentTimeMillis() - 10000) {
                // Player logged in less than 10 seconds ago, might be still doing handshake
                if (sendQueue.containsKey(comm.player)) {
                    sendQueue.get(comm.player).add(bb);
                } else {
                    LinkedList<ByteBuffer> list = new LinkedList<>();
                    list.add(bb);
                    sendQueue.put(comm.player, list);
                }
            }
        }
    }

    public static Communicator[] getValidRecipients(VolaTile tile)
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

    public static VolaTile getTileIfGroundItem(DbItem item)
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
