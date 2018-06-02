package com.armdroid.rxfilechooser.exception;


import com.tbruyelle.rxpermissions2.Permission;

public class PermissionDeniedException extends Exception {

    private final Permission mPermission;

    public PermissionDeniedException(Permission permission) {
        super("You don't have '" + permission.name + "' permission");
        mPermission = permission;
    }

    public Permission getPermission() {
        return mPermission;
    }
}
