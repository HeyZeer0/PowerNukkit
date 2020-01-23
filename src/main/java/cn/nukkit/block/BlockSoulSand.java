package cn.nukkit.block;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.block.BlockFormEvent;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.Identifier;

/**
 * Created by Pub4Game on 27.12.2015.
 */
public class BlockSoulSand extends BlockSolid {

    public BlockSoulSand(Identifier id) {
        super(id);
    }

    @Override
    public double getHardness() {
        return 0.5;
    }

    @Override
    public double getResistance() {
        return 2.5;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_SHOVEL;
    }

    @Override
    public double getMaxY() {
        return this.y + 1 - 0.125;
    }

    @Override
    public boolean hasEntityCollision() {
        return true;
    }

    @Override
    public void onEntityCollide(Entity entity) {
        entity.setMotion(entity.getMotion().multiply(0.4, 1, 0.4));
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            Block up = up();
            if (up instanceof BlockWater && (up.getDamage() == 0 || up.getDamage() == 8)) {
                BlockFormEvent event = new BlockFormEvent(up, new BlockBubbleColumn(0));
                if (!event.isCancelled()) {
                    if (event.getNewState().getWaterloggingLevel() > 0) {
                        this.getLevel().setBlock(up, 1, new BlockWater(), true, false);
                    }
                    this.getLevel().setBlock(up, 0, event.getNewState(), true, true);
                }
            }
        }
        return 0;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BROWN_BLOCK_COLOR;
    }

}
