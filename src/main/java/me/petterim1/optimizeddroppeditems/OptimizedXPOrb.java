package me.petterim1.optimizeddroppeditems;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class OptimizedXPOrb extends EntityXPOrb {

    private int age;
    private int pickupDelay;
    private int exp;

    public OptimizedXPOrb(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public String getName() {
        return "XP";
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

        if (this.age > 4800) {
            this.close();
            return false;
        }

        this.entityBaseTick(tickDiff);

        if (this.isAlive()) {
            if (this.pickupDelay > 0) {
                this.pickupDelay -= tickDiff;
                if (this.pickupDelay < 0) {
                    this.pickupDelay = 0;
                }
            } else {
                Entity[] e = this.level.getCollidingEntities(this.boundingBox, this);
                for (Entity entity : e) {
                    if (entity instanceof Player) {
                        if (((Player) entity).pickupEntity(this, false)) {
                            return true;
                        }
                    }
                }
            }

            if (!this.isOnGround()) {
                this.motionY -= this.getGravity();
            }

            if (this.closestPlayer == null || this.closestPlayer.distanceSquared(this) > 64.0D) {
                for (Player p : level.getPlayers().values()) {
                    if (!p.isSpectator() && p.distance(this) <= 8) {
                        this.closestPlayer = p;
                        break;
                    }
                }
            }

            if (this.closestPlayer != null) {
                if (this.closestPlayer.isSpectator()) {
                    this.closestPlayer = null;
                } else {
                    double dX = (this.closestPlayer.x - this.x) / 8.0D;
                    double dY = (this.closestPlayer.y + (double) this.closestPlayer.getEyeHeight() / 2.0D - this.y) / 8.0D;
                    double dZ = (this.closestPlayer.z - this.z) / 8.0D;
                    double d = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
                    double diff = 1.0D - d;

                    if (diff > 0.0D) {
                        diff = diff * diff;
                        this.motionX += dX / d * diff * 0.1D;
                        this.motionY += dY / d * diff * 0.1D;
                        this.motionZ += dZ / d * diff * 0.1D;
                    }
                }
            }

            this.move(this.motionX, this.motionY, this.motionZ);

            this.motionX *= 0.9;
            this.motionY *= 0.98;
            this.motionZ *= 0.9;

            if (this.onGround) {
                this.motionY *= -0.5;
            }

            this.updateMovement();
            return true;
        }

        return false;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        this.blocksAround = null;
        this.collisionBlocks = null;
        this.justCreated = false;

        if (!this.isAlive()) {
            this.despawnFromAll();
            this.close();
            return false;
        } else if (this.closed) {
            this.despawnFromAll();
            return false;
        } else {
            if (this.y < 0 || this.fireTicks > 0) {
                this.despawnFromAll();
                this.close();
                return false;
            }

            this.age += tickDiff;
            return true;
        }
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(5);
        this.setHealth(5.0F);
        if (this.namedTag.contains("Health")) {
            this.setHealth(this.namedTag.getShort("Health"));
        }

        if (this.namedTag.contains("Age")) {
            this.age = this.namedTag.getShort("Age");
        }

        if (this.namedTag.contains("PickupDelay")) {
            this.pickupDelay = this.namedTag.getShort("PickupDelay");
        }

        if (this.namedTag.contains("Value")) {
            this.exp = this.namedTag.getShort("Value");
        }

        if (this.exp <= 0) {
            this.exp = 1;
        }

        this.dataProperties.putInt(DATA_EXPERIENCE_VALUE, this.exp);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putShort("Health", (int) getHealth());
        this.namedTag.putShort("Age", age);
        this.namedTag.putShort("PickupDelay", pickupDelay);
        this.namedTag.putShort("Value", exp);
    }

    @Override
    public int getExp() {
        return exp;
    }

    @Override
    public void setExp(int exp) {
        if (exp <= 0) {
            throw new IllegalArgumentException("XP amount must be greater than 0, got " + exp);
        }
        this.exp = exp;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public int getPickupDelay() {
        return pickupDelay;
    }

    @Override
    public void setPickupDelay(int pickupDelay) {
        this.pickupDelay = pickupDelay;
    }
}
