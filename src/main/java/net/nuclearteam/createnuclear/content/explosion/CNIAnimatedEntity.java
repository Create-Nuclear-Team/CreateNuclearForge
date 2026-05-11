package net.nuclearteam.createnuclear.content.explosion;


public interface CNIAnimatedEntity {
    CNAnimation NO_ANIMATION = CNAnimation.create(0);

    int getAnimationTick();

    void setAnimationTick(int var1);

    CNAnimation getAnimation();

    void setAnimation(CNAnimation var1);

    CNAnimation[] getAnimations();
}
