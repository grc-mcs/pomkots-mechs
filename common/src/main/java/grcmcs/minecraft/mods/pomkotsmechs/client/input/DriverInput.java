package grcmcs.minecraft.mods.pomkotsmechs.client.input;

public class DriverInput {
    private short keyPressStatus = 0;

    public DriverInput(short st) {
        this.keyPressStatus = st;
    }

    public short getStatus() {
        return keyPressStatus;
    }

    public boolean isWeaponRightHandPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.WEAPON_ARM_R.getKeyID()) != 0;
    }

    public boolean isWeaponLeftHandPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.WEAPON_ARM_L.getKeyID()) != 0;
    }

    public boolean isWeaponRightShoulderPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.WEAPON_SHOULDER_R.getKeyID()) != 0;
    }

    public boolean isWeaponLeftShoulderPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.WEAPON_SHOULDER_L.getKeyID()) != 0;
    }

    public boolean isLockPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.LOCK.getKeyID()) != 0;
    }

    public boolean isEvasionPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.EVASION.getKeyID()) != 0;
    }

    public boolean isModeChangePressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.MODE.getKeyID()) != 0;
    }

    public boolean isForwardPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.FORWARD.getKeyID()) != 0;
    }

    public boolean isBackPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.BACK.getKeyID()) != 0;
    }

    public boolean isRightPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.RIGHT.getKeyID()) != 0;
    }

    public boolean isLeftPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.LEFT.getKeyID()) != 0;
    }

    public boolean isJumpPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.JUMP.getKeyID()) != 0;
    }

    @Override
    public String toString() {
        return "key status:" + keyPressStatus;
    }
}