package net.gegy1000.terrarium.server.util;

public class FlipFlopTimer {
    private final float speed;

    private float value;
    private Side target = Side.LEFT;
    private boolean moved;

    public FlipFlopTimer(float speed) {
        this.speed = speed;
    }

    public void update(float deltaTime) {
        if (!this.target.isSettled(this.value)) {
            this.value = this.target.update(this.value, deltaTime * this.speed);
        }
    }

    public void setTarget(Side target) {
        this.target = target;
        this.moved = true;
    }

    public float getValue() {
        return this.value;
    }

    public Side getTarget() {
        return this.target;
    }

    public boolean isLeft() {
        return this.target == Side.LEFT && this.isSettled();
    }

    public boolean isRight() {
        return this.target == Side.RIGHT && this.isSettled();
    }

    public boolean isSettled() {
        return this.target.isSettled(this.value);
    }

    public boolean hasMoved() {
        return this.moved;
    }

    public enum Side {
        LEFT {
            @Override
            protected float update(float value, float delta) {
                return Math.max(value - delta, 0.0F);
            }

            @Override
            protected boolean isSettled(float value) {
                return value <= 0.0F;
            }
        },
        RIGHT {
            @Override
            protected float update(float value, float delta) {
                return Math.min(value + delta, 1.0F);
            }

            @Override
            protected boolean isSettled(float value) {
                return value >= 1.0F;
            }
        };

        protected abstract float update(float value, float delta);

        protected abstract boolean isSettled(float value);
    }
}
