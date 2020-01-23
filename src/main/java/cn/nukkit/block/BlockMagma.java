package cn.nukkit.block;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.block.BlockFormEvent;
import cn.nukkit.event.entity.EntityDamageByBlockEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.player.Player;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.Identifier;

public class BlockMagma extends BlockSolid {

    public BlockMagma(Identifier id) {
        super(id);
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public double getHardness() {
        return 0.5;
    }

    @Override
    public double getResistance() {
        return 30;
    }

    @Override
    public int getLightLevel() {
        return 3;
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.isPickaxe() && item.getTier() >= ItemTool.TIER_WOODEN) {
            return new Item[]{
                    toItem()
            };
        } else {
            return new Item[0];
        }
    }

    @Override
    public void onEntityCollide(Entity entity) {
        if (!entity.hasEffect(Effect.FIRE_RESISTANCE)) {
            if (entity instanceof Player) {
                Player p = (Player) entity;
                if (!p.isCreative() && !p.isSpectator() && !p.isSneaking()) {
                    entity.attack(new EntityDamageByBlockEvent(this, entity, EntityDamageEvent.DamageCause.LAVA, 1));
                }
            } else {
                entity.attack(new EntityDamageByBlockEvent(this, entity, EntityDamageEvent.DamageCause.LAVA, 1));
            }
        }
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            Block up = up();
            if (up instanceof BlockWater && (up.getDamage() == 0 || up.getDamage() == 8)) {
                BlockFormEvent event = new BlockFormEvent(up, new BlockBubbleColumn(1));
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
        return BlockColor.NETHERRACK_BLOCK_COLOR;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

}
