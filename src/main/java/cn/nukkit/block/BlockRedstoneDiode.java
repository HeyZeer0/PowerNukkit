package cn.nukkit.block;

import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3f;
import cn.nukkit.math.Vector3i;
import cn.nukkit.player.Player;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.Faceable;
import cn.nukkit.utils.Identifier;

import static cn.nukkit.block.BlockIds.*;

/**
 * @author CreeperFace
 */
public abstract class BlockRedstoneDiode extends FloodableBlock implements Faceable {

    protected boolean isPowered = false;

    public BlockRedstoneDiode(Identifier id) {
        super(id);
    }

    @Override
    public int getWaterloggingLevel() {
        return 2;
    }

    @Override
    public boolean canBeFlowedInto() {
        return false;
    }

    @Override
    public boolean onBreak(Item item) {
        Vector3i pos = asVector3i();
        this.level.setBlock(this, Block.get(AIR), true, true);

        for (BlockFace face : BlockFace.values()) {
            this.level.updateAroundRedstone(pos.getSide(face), null);
        }
        return true;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, Vector3f clickPos, Player player) {
        if (block.getSide(BlockFace.DOWN).isTransparent()) {
            return false;
        }

        this.setDamage(player != null ? player.getDirection().getOpposite().getHorizontalIndex() : 0);
        this.level.setBlock(block, this, true, true);

        if (shouldBePowered()) {
            this.level.scheduleUpdate(this, 1);
        }
        return true;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (!this.isLocked()) {
                Vector3i pos = asVector3i();
                boolean shouldBePowered = this.shouldBePowered();

                if (this.isPowered && !shouldBePowered) {
                    this.level.setBlock(pos, this.getUnpowered(), true, true);

                    this.level.updateAroundRedstone(this.asVector3i().getSide(getFacing().getOpposite()), null);
                } else if (!this.isPowered) {
                    this.level.setBlock(pos, this.getPowered(), true, true);
                    this.level.updateAroundRedstone(this.asVector3i().getSide(getFacing().getOpposite()), null);

                    if (!shouldBePowered) {
//                        System.out.println("schedule update 2");
                        level.scheduleUpdate(getPowered(), this, this.getDelay());
                    }
                }
            }
        } else if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
            // Redstone event
            RedstoneUpdateEvent ev = new RedstoneUpdateEvent(this);
            getLevel().getServer().getPluginManager().callEvent(ev);
            if (ev.isCancelled()) {
                return 0;
            }
            if (type == Level.BLOCK_UPDATE_NORMAL && this.getSide(BlockFace.DOWN).isTransparent()) {
                this.level.useBreakOn(this);
                return Level.BLOCK_UPDATE_NORMAL;
            } else {
                this.updateState();
                return Level.BLOCK_UPDATE_NORMAL;
            }
        }
        return 0;
    }

    public void updateState() {
        if (!this.isLocked()) {
            boolean shouldPowered = this.shouldBePowered();

            if ((this.isPowered && !shouldPowered || !this.isPowered && shouldPowered) && !this.level.isBlockTickPending(this, this)) {
                /*int priority = -1;

                if (this.isFacingTowardsRepeater()) {
                    priority = -3;
                } else if (this.isPowered) {
                    priority = -2;
                }*/

                this.level.scheduleUpdate(this, this, this.getDelay());
            }
        }
    }

    public boolean isLocked() {
        return false;
    }

    protected int calculateInputStrength() {
        BlockFace face = getFacing();
        Vector3i pos = this.asVector3i().getSide(face);
        int power = this.level.getRedstonePower(pos, face);

        if (power >= 15) {
            return power;
        } else {
            Block block = this.level.getBlock(pos);
            return Math.max(power, block.getId() == REDSTONE_WIRE ? block.getDamage() : 0);
        }
    }

    protected int getPowerOnSides() {
        Vector3i pos = asVector3i();

        BlockFace face = getFacing();
        BlockFace face1 = face.rotateY();
        BlockFace face2 = face.rotateYCCW();
        return Math.max(this.getPowerOnSide(pos.getSide(face1), face1), this.getPowerOnSide(pos.getSide(face2), face2));
    }

    protected int getPowerOnSide(Vector3i pos, BlockFace side) {
        Block block = this.level.getBlock(pos);
        return isAlternateInput(block) ? (block.getId() == REDSTONE_BLOCK ? 15 : (block.getId() == REDSTONE_WIRE ? block.getDamage() : this.level.getStrongPower(pos, side))) : 0;
    }

    @Override
    public boolean isPowerSource() {
        return true;
    }

    protected boolean shouldBePowered() {
        return this.calculateInputStrength() > 0;
    }

    public abstract BlockFace getFacing();

    protected abstract int getDelay();

    protected abstract Block getUnpowered();

    protected abstract Block getPowered();

    @Override
    public double getMaxY() {
        return this.y + 0.125;
    }

    @Override
    public boolean canPassThrough() {
        return false;
    }

    protected boolean isAlternateInput(Block block) {
        return block.isPowerSource();
    }

    public static boolean isDiode(Block block) {
        return block instanceof BlockRedstoneDiode;
    }

    protected int getRedstoneSignal() {
        return 15;
    }

    public int getStrongPower(BlockFace side) {
        return getWeakPower(side);
    }

    public int getWeakPower(BlockFace side) {
        return !this.isPowered() ? 0 : (getFacing() == side ? this.getRedstoneSignal() : 0);
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    public boolean isPowered() {
        return isPowered;
    }

    public boolean isFacingTowardsRepeater() {
        BlockFace side = getFacing().getOpposite();
        Block block = this.getSide(side);
        return block instanceof BlockRedstoneDiode && ((BlockRedstoneDiode) block).getFacing() != side;
    }

    @Override
    public BlockFace getBlockFace() {
        return BlockFace.fromHorizontalIndex(this.getDamage() & 0x07);
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.AIR_BLOCK_COLOR;
    }
}
