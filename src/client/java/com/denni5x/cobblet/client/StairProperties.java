package com.denni5x.cobblet.client;

import net.minecraft.block.enums.StairShape;
import net.minecraft.util.math.Direction;

public record StairProperties(Direction direction, StairShape shape) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StairProperties)) return false;
        StairProperties that = (StairProperties) o;
        return direction.equals(that.direction) && shape.equals(that.shape);
    }

}