package com.ldtteam.perviaminvenire.handlers;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.ExtendedNode;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID)
public class SpiderClimbableTickEventHandler
{

    @SubscribeEvent
    public static void onLivingUpdate(final LivingEvent.LivingUpdateEvent event)
    {
        if (!(event.getEntity() instanceof final Spider spiderEntity)) {
            return;
        }

        if (!(spiderEntity.getNavigation() instanceof final AbstractAdvancedGroundPathNavigator navigator))
            return;

        if (navigator.isInProgress() || navigator.getPath() == null) {
            spiderEntity.setClimbing(false);
            return;
        }

        final Path path = navigator.getPath();
        final Node pathPoint = path.getNextNode();
        if (!(pathPoint instanceof final ExtendedNode pathPointPreviousExtended)) {
            spiderEntity.setClimbing(false);
            return;
        }

        final ExtendedNode pathPointExtended = (ExtendedNode) pathPoint;
        final boolean currentIsLadder = pathPointExtended.isOnLadder();
        if (path.getNextNodeIndex() == 0)
        {
            spiderEntity.setClimbing(currentIsLadder);
            return;
        }

        final Node pathPointPrevious = path.getNode(path.getNextNodeIndex() - 1);
        if (!(pathPointPrevious instanceof ExtendedNode)) {
            spiderEntity.setClimbing(currentIsLadder);
            return;
        }
        final boolean previousIsLadder = pathPointPreviousExtended.isOnLadder();

        spiderEntity.setClimbing(previousIsLadder || currentIsLadder);
    }
}
