/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.gecko.util;

import org.mozilla.gecko.AppConstants;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.system.Os;
import android.util.Log;

public final class HardwareUtils {
    private static final String LOGTAG = "GeckoHardwareUtils";

    private static final boolean IS_AMAZON_DEVICE = Build.MANUFACTURER.equalsIgnoreCase("Amazon");
    public static final boolean IS_KINDLE_DEVICE = IS_AMAZON_DEVICE &&
                                                   (Build.MODEL.equals("Kindle Fire") ||
                                                    Build.MODEL.startsWith("KF"));

    private static volatile boolean sInited;

    // These are all set once, during init.
    private static volatile boolean sIsLargeTablet;
    private static volatile boolean sIsSmallTablet;
    private static volatile boolean sIsTelevision;

    private HardwareUtils() {
    }

    public static void init(Context context) {
        if (sInited) {
            // This is unavoidable, given that HardwareUtils is called from background services.
            Log.d(LOGTAG, "HardwareUtils already inited.");
            return;
        }

        // Pre-populate common flags from the context.
        final int screenLayoutSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (Build.VERSION.SDK_INT >= 11) {
            if (screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                sIsLargeTablet = true;
            } else if (screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                sIsSmallTablet = true;
            }
            if (Build.VERSION.SDK_INT >= 16) {
                if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEVISION)) {
                    sIsTelevision = true;
                }
            }
        }

        sInited = true;
    }

    public static boolean isTablet() {
        return sIsLargeTablet || sIsSmallTablet;
    }

    public static boolean isLargeTablet() {
        return sIsLargeTablet;
    }

    public static boolean isSmallTablet() {
        return sIsSmallTablet;
    }

    public static boolean isTelevision() {
        return sIsTelevision;
    }

    public static boolean isARMSystem() {
        return Build.CPU_ABI != null && Build.CPU_ABI.equals("armeabi-v7a");
    }

    public static boolean isX86System() {
        if (Build.CPU_ABI != null && Build.CPU_ABI.equals("x86")) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            // On some devices we have to look into the kernel release string.
            try {
                return Os.uname().release.contains("-x86_");
            } catch (final Exception e) {
                Log.w(LOGTAG, "Cannot get uname", e);
            }
        }
        return false;
    }

    public static String getRealAbi() {
        if (isX86System() && isARMSystem()) {
            // Some x86 devices try to make us believe we're ARM,
            // in which case CPU_ABI is not reliable.
            return "x86";
        }
        return Build.CPU_ABI;
    }
}
