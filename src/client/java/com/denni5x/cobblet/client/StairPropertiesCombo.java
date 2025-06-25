package com.denni5x.cobblet.client;

public record StairPropertiesCombo(StairProperties stair1, StairProperties stair2) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StairPropertiesCombo that)) return false;
        return stair1.equals(that.stair1) && stair2.equals(that.stair2);
    }

}