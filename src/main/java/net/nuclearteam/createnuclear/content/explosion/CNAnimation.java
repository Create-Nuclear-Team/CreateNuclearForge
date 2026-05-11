package net.nuclearteam.createnuclear.content.explosion;

public class CNAnimation {
    /** @deprecated */
    @Deprecated
    private int id;
    private int duration;

    private CNAnimation(int duration) {
        this.duration = duration;
    }

    /** @deprecated */
    @Deprecated
    public static CNAnimation create(int id, int duration) {
        CNAnimation animation = create(duration);
        animation.id = id;
        return animation;
    }

    public static CNAnimation create(int duration) {
        return new CNAnimation(duration);
    }

    /** @deprecated */
    @Deprecated
    public int getID() {
        return this.id;
    }

    public int getDuration() {
        return this.duration;
    }
}
