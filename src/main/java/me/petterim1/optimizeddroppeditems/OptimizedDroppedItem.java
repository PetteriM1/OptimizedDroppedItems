package me.petterim1.optimizeddroppeditems;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;

public class OptimizedDroppedItem extends EntityItem {

    public OptimizedDroppedItem(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public String getName() {
        return "OptimizedDroppedItem";
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        int tickDiff = currentTick - this.lastUpdate;

        if (tickDiff <= 0 && !this.justCreated) {
            return true;
        }

        this.lastUpdate = currentTick;

        if (this.age > 4800 || this.isInsideOfFire()) {
            this.close();
            return true;
        }

        boolean hasUpdate = this.entityBaseTick(tickDiff);

        if (this.isAlive()) {
            Entity[] e = this.getLevel().getNearbyEntities(getBoundingBox().grow(1, 1, 1), this, false);

            if (this.pickupDelay > 0 && this.pickupDelay < 32767) {
                this.pickupDelay -= tickDiff;
                if (this.pickupDelay < 0) {
                    this.pickupDelay = 0;
                }
            } else {
                for (Entity entity : e) {
                    if (entity instanceof Player) {
                        if (((Player) entity).pickupEntity(this, true)) {
                            return true;
                        }
                    }
                }
            }

            if (this.age % 200 == 0 && this.onGround && this.item != null) {
                if (this.item.getCount() < this.item.getMaxStackSize()) {
                    for (Entity entity : e) {
                        if (entity instanceof EntityItem) {
                            if (!entity.isAlive()) {
                                continue;
                            }
                            Item closeItem = ((EntityItem) entity).getItem();
                            if (!closeItem.equals(item, true, true)) {
                                continue;
                            }
                            if (!entity.isOnGround()) {
                                continue;
                            }
                            int newAmount = this.item.getCount() + closeItem.getCount();
                            if (newAmount > this.item.getMaxStackSize()) {
                                continue;
                            }
                            closeItem.setCount(0);
                            entity.close();
                            this.item.setCount(newAmount);
                            EntityEventPacket packet = new EntityEventPacket();
                            packet.eid = getId();
                            packet.data = newAmount;
                            packet.event = EntityEventPacket.MERGE_ITEMS;
                            Server.broadcastPacket(this.getLevel().getPlayers().values(), packet);
                        }
                    }
                }
            }

            if (this.isInsideOfWater()) {
                this.motionY = -0.02;
            } else if (!this.isOnGround()) {
                this.motionY -= 0.04;
            }

            this.move(this.motionX, this.motionY, this.motionZ);

            this.motionX *= 0.9;
            this.motionY *= 0.98;
            this.motionZ *= 0.9;

            if (this.onGround) {
                this.motionY *= -0.5;
            }

            this.updateMovement();
        }

        return hasUpdate || !this.onGround || Math.abs(this.motionX) > 0.00001 || Math.abs(this.motionY) > 0.00001 || Math.abs(this.motionZ) > 0.00001;
    }
}
