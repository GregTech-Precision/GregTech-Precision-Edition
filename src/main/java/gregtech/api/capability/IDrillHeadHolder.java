package gregtech.api.capability;

public interface IDrillHeadHolder {

    boolean hasDrillHead();

    int getOrePerCycle();

    void applyDrillHeadDamage(int damageApplied);
}
