package com.vikingsen.randomdirection;

import android.support.annotation.StringRes;

public enum Direction {
    UNKNOWN(-1, R.string.unknown),
    NORTH(0, R.string.north),
    EAST(90, R.string.east),
    SOUTH(180, R.string.south),
    WEST(270, R.string.west);

    private final float degrees;
    private final int nameId;

    Direction(float degrees, @StringRes int nameId) {
        this.degrees = degrees;
        this.nameId = nameId;
    }

    public float getDegrees() {
        return degrees;
    }

    public int getNameId() {
        return nameId;
    }
}
