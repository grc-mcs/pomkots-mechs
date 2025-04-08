package grcmcs.minecraft.mods.pomkotsmechs.client.input;

public class DriverInput {
    private short keyPressStatus = 0;
    private DriverInput prevInput = null;

    public DriverInput(short st) {
        this(st, null);
    }

    public DriverInput(short st, DriverInput prev) {
        this.keyPressStatus = st;
        this.prevInput = prev;
    }

    public short getStatus() {
        return keyPressStatus;
    }

    public boolean isWeaponRightHandPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.WEAPON_ARM_R.getKeyID()) != 0;
    }

    public boolean isWeaponRightHandReleased() {
        if (this.prevInput != null) {
            return ((this.prevInput.keyPressStatus & UserInteractionManager.Keys.WEAPON_ARM_R.getKeyID()) != 0)
                    && ((this.keyPressStatus & UserInteractionManager.Keys.WEAPON_ARM_R.getKeyID()) == 0);
        } else {
            return false;
        }
    }

    public boolean isWeaponLeftHandPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.WEAPON_ARM_L.getKeyID()) != 0;
    }

    public boolean isWeaponLeftHandReleased() {
        if (this.prevInput != null) {
            return ((this.prevInput.keyPressStatus & UserInteractionManager.Keys.WEAPON_ARM_L.getKeyID()) != 0)
                    && ((this.keyPressStatus & UserInteractionManager.Keys.WEAPON_ARM_L.getKeyID()) == 0);
        } else {
            return false;
        }
    }

    public boolean isWeaponRightShoulderPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.WEAPON_SHOULDER_R.getKeyID()) != 0;
    }

    public boolean isWeaponRightShoulderReleased() {
        if (this.prevInput != null) {
            return ((this.prevInput.keyPressStatus & UserInteractionManager.Keys.WEAPON_SHOULDER_R.getKeyID()) != 0)
                    && ((this.keyPressStatus & UserInteractionManager.Keys.WEAPON_SHOULDER_R.getKeyID()) == 0);
        } else {
            return false;
        }
    }

    public boolean isWeaponLeftShoulderPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.WEAPON_SHOULDER_L.getKeyID()) != 0;
    }

    public boolean isWeaponLeftShoulderReleased() {
        if (this.prevInput != null) {
            return ((this.prevInput.keyPressStatus & UserInteractionManager.Keys.WEAPON_SHOULDER_L.getKeyID()) != 0)
                    && ((this.keyPressStatus & UserInteractionManager.Keys.WEAPON_SHOULDER_L.getKeyID()) == 0);
        } else {
            return false;
        }
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