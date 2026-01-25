package net.nuclearteam.createnuclear.content.explosion;

import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CNModelAnimator {
    private int tempTick = 0;
    private int prevTempTick;
    private boolean correctAnimation = false;
    private CNIAnimatedEntity entity;
    private HashMap<CNAdvancedModelBox, CNTransform> CNTransformMap = new HashMap();
    private HashMap<CNAdvancedModelBox, CNTransform> prevCNTransformMap = new HashMap();

    public static CNModelAnimator create() {
        return new CNModelAnimator();
    }

    public CNIAnimatedEntity getEntity() {
        return this.entity;
    }

    public void update(CNIAnimatedEntity entity) {
        this.tempTick = this.prevTempTick = 0;
        this.correctAnimation = false;
        this.entity = entity;
        this.CNTransformMap.clear();
        this.prevCNTransformMap.clear();
    }

    public boolean setAnimation(CNAnimation animation) {
        this.tempTick = this.prevTempTick = 0;
        this.correctAnimation = this.entity.getAnimation() == animation;
        return this.correctAnimation;
    }

    public void startKeyframe(int duration) {
        if (this.correctAnimation) {
            this.prevTempTick = this.tempTick;
            this.tempTick += duration;
        }
    }

    public void setStaticKeyframe(int duration) {
        this.startKeyframe(duration);
        this.endKeyframe(true);
    }

    public void resetKeyframe(int duration) {
        this.startKeyframe(duration);
        this.endKeyframe();
    }

    public void rotate(CNAdvancedModelBox box, float x, float y, float z) {
        if (this.correctAnimation) {
            this.getCNTransform(box).addRotation(x, y, z);
        }
    }

    public void move(CNAdvancedModelBox box, float x, float y, float z) {
        if (this.correctAnimation) {
            this.getCNTransform(box).addOffset(x, y, z);
        }
    }

    private CNTransform getCNTransform(CNAdvancedModelBox box) {
        return (CNTransform)this.CNTransformMap.computeIfAbsent(box, (b) -> new CNTransform());
    }

    public void endKeyframe() {
        this.endKeyframe(false);
    }

    private void endKeyframe(boolean stationary) {
        if (this.correctAnimation) {
            int animationTick = this.entity.getAnimationTick();
            if (animationTick >= this.prevTempTick && animationTick < this.tempTick) {
                if (stationary) {
                    for(CNAdvancedModelBox box : this.prevCNTransformMap.keySet()) {
                        CNTransform CNTransform = (CNTransform)this.prevCNTransformMap.get(box);
                        box.rotateAngleX += CNTransform.getRotationX();
                        box.rotateAngleY += CNTransform.getRotationY();
                        box.rotateAngleZ += CNTransform.getRotationZ();
                        box.rotationPointX += CNTransform.getOffsetX();
                        box.rotationPointY += CNTransform.getOffsetY();
                        box.rotationPointZ += CNTransform.getOffsetZ();
                    }
                } else {
                    float frameTime = Minecraft.getInstance().isPaused() ? 0.0F : Minecraft.getInstance().getFrameTime();
                    float tick = ((float)(animationTick - this.prevTempTick) + frameTime) / (float)(this.tempTick - this.prevTempTick);
                    float inc = Mth.sin((float)((double)tick * Math.PI / (double)2.0F));
                    float dec = 1.0F - inc;

                    for(CNAdvancedModelBox box : this.prevCNTransformMap.keySet()) {
                        CNTransform CNTransform = (CNTransform)this.prevCNTransformMap.get(box);
                        box.rotateAngleX += dec * CNTransform.getRotationX();
                        box.rotateAngleY += dec * CNTransform.getRotationY();
                        box.rotateAngleZ += dec * CNTransform.getRotationZ();
                        box.rotationPointX += dec * CNTransform.getOffsetX();
                        box.rotationPointY += dec * CNTransform.getOffsetY();
                        box.rotationPointZ += dec * CNTransform.getOffsetZ();
                    }

                    for(CNAdvancedModelBox box : this.CNTransformMap.keySet()) {
                        CNTransform CNTransform = (CNTransform)this.CNTransformMap.get(box);
                        box.rotateAngleX += inc * CNTransform.getRotationX();
                        box.rotateAngleY += inc * CNTransform.getRotationY();
                        box.rotateAngleZ += inc * CNTransform.getRotationZ();
                        box.rotationPointX += inc * CNTransform.getOffsetX();
                        box.rotationPointY += inc * CNTransform.getOffsetY();
                        box.rotationPointZ += inc * CNTransform.getOffsetZ();
                    }
                }
            }

            if (!stationary) {
                this.prevCNTransformMap.clear();
                this.prevCNTransformMap.putAll(this.CNTransformMap);
                this.CNTransformMap.clear();
            }

        }
    }
}