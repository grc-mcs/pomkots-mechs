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

    public boolean isExtension1Pressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.EXT1.getKeyID()) != 0;
    }

    public boolean isExtension1Released() {
        if (this.prevInput != null) {
            return ((this.prevInput.keyPressStatus & UserInteractionManager.Keys.EXT1.getKeyID()) != 0)
                    && ((this.keyPressStatus & UserInteractionManager.Keys.EXT1.getKeyID()) == 0);
        } else {
            return false;
        }
    }

    public boolean isExtension2Pressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.EXT2.getKeyID()) != 0;
    }

    public boolean isExtension2Released() {
        if (this.prevInput != null) {
            return ((this.prevInput.keyPressStatus & UserInteractionManager.Keys.EXT2.getKeyID()) != 0)
                    && ((this.keyPressStatus & UserInteractionManager.Keys.EXT2.getKeyID()) == 0);
        } else {
            return false;
        }
    }

    public boolean isReloadPressed() {
        return (this.keyPressStatus & UserInteractionManager.Keys.RELOAD.getKeyID()) != 0;
    }

    public boolean isReloadReleased() {
        if (this.prevInput != null) {
            return ((this.prevInput.keyPressStatus & UserInteractionManager.Keys.RELOAD.getKeyID()) != 0)
                    && ((this.keyPressStatus & UserInteractionManager.Keys.RELOAD.getKeyID()) == 0);
        } else {
            return false;
        }
    }

    public boolean isRepairReleased() {
        if (this.prevInput != null) {
            return ((this.prevInput.keyPressStatus & UserInteractionManager.Keys.REPAIR.getKeyID()) != 0)
                    && ((this.keyPressStatus & UserInteractionManager.Keys.REPAIR.getKeyID()) == 0);
        } else {
            return false;
        }
    }

    private void setKey(int keyId, boolean pressed) {
        if (pressed) {
            keyPressStatus |= keyId;
        } else {
            keyPressStatus &= ~keyId;
        }
    }

    public void setWeaponRightHandPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.WEAPON_ARM_R.getKeyID(),
                pressed
        );
    }

    public void setWeaponLeftHandPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.WEAPON_ARM_L.getKeyID(),
                pressed
        );
    }

    public void setWeaponRightShoulderPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.WEAPON_SHOULDER_R.getKeyID(),
                pressed
        );
    }

    public void setWeaponLeftShoulderPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.WEAPON_SHOULDER_L.getKeyID(),
                pressed
        );
    }

    public void setForwardPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.FORWARD.getKeyID(),
                pressed
        );
    }

    public void setBackPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.BACK.getKeyID(),
                pressed
        );
    }

    public void setRightPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.RIGHT.getKeyID(),
                pressed
        );
    }

    public void setLeftPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.LEFT.getKeyID(),
                pressed
        );
    }

    public void setJumpPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.JUMP.getKeyID(),
                pressed
        );
    }

    public void setEvasionPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.EVASION.getKeyID(),
                pressed
        );
    }

    public void setLockPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.LOCK.getKeyID(),
                pressed
        );
    }

    public void setModeChangePressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.MODE.getKeyID(),
                pressed
        );
    }

    public void setReloadPressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.RELOAD.getKeyID(),
                pressed
        );
    }

    public void setExtension1Pressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.EXT1.getKeyID(),
                pressed
        );
    }

    public void setExtension2Pressed(boolean pressed) {
        setKey(
                UserInteractionManager.Keys.EXT2.getKeyID(),
                pressed
        );
    }

    @Override
    public String toString() {
        return "key status:" + keyPressStatus;
    }
}