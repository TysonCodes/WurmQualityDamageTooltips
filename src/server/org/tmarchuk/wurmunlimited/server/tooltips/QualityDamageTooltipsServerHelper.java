package org.tmarchuk.wurmunlimited.server.tooltips;

/**
 * Created by Tyson Marchuk on 2016-04-24.
 */

// From Wurm Unlimited Dedicated Server
import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.DbItem;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

// Base Java
import java.util.ArrayList;

public class QualityDamageTooltipsServerHelper
{
    private static final String STRUCTURE_QUALITY_WINDOW_TITLE = ":mod:structure_quality_info";
    private static final String STRUCTURE_DAMAGE_WINDOW_TITLE = ":mod:structure_damage_info";
    private static final String ITEM_QUALITY_WINDOW_TITLE = ":mod:item_quality_info";
    private static final String ITEM_DAMAGE_WINDOW_TITLE = ":mod:item_damage_info";

    // Send quality and damage of a structure to client in hidden chat tab.
    public static void sendStructureQualityAndDamage(Communicator comm, long id, float quality, float damage)
    {
        sendData(comm, id, quality, STRUCTURE_QUALITY_WINDOW_TITLE);
        sendData(comm, id, damage, STRUCTURE_DAMAGE_WINDOW_TITLE);
    }

    // Send quality of a structure to client in hidden chat tab.
    public static void sendStructureQuality(Communicator comm, long id, float quality)
    {
        sendData(comm, id, quality, STRUCTURE_QUALITY_WINDOW_TITLE);
    }

    // Send damage of a structure to client in hidden chat tab.
    public static void sendStructureDamage(Communicator comm, long id, float damage)
    {
        sendData(comm, id, damage, STRUCTURE_DAMAGE_WINDOW_TITLE);
    }

    // Send quality of an item to client in hidden chat tab.
    public static void sendItemQuality(Communicator comm, long id, float quality)
    {
        sendData(comm, id, quality, ITEM_QUALITY_WINDOW_TITLE);
    }

    // Send damage of an item to client in hidden chat tab.
    public static void sendItemDamage(Communicator comm, long id, float damage)
    {
        sendData(comm, id, damage, ITEM_DAMAGE_WINDOW_TITLE);
    }

    // Send data about an item or structure to a private chat channel.
    public static void sendData(Communicator comm, long id, float value, String window)
    {
        Message theMsg = new Message(comm.getPlayer(), (byte) 10, window, id + ":" + value);
        comm.sendMessage(theMsg);
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
