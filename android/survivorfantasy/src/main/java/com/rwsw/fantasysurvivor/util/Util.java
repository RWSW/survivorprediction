package com.rwsw.fantasysurvivor.util;

import android.content.Context;
import android.content.pm.PackageManager;
import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Util {

    public static Boolean hasCamera() {
        PackageManager pm = FantasySurvivor.getContext().getPackageManager();
        boolean frontCam, rearCam;

        //Must have a targetSdk >= 9 defined in the AndroidManifest
        frontCam = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        rearCam = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);

        return (frontCam || rearCam);
    }

    public static boolean hasSDCard() {
        return android.os.Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static File getTempFile(Context context, String folderNameInCacheDir) {
        File file = null;
        try {
            String fileName = UUID.randomUUID().toString();
            StringBuilder sb = new StringBuilder();
            File cacheDir = null;
            sb.append(context.getCacheDir().getAbsolutePath());
            if (!folderNameInCacheDir.isEmpty()) {
                sb.append("/");
                sb.append(folderNameInCacheDir);
                sb.append("/");
                cacheDir = new File(sb.toString());
                cacheDir.mkdirs();
            } else {
                cacheDir = new File(sb.toString());
            }
            file = File.createTempFile(fileName, null, cacheDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static String getLocalDate(){
        String result = "";
        try {
            // Formatter with message list view pattern
            DateTimeFormatter fmtToShow = DateTimeFormat.shortDateTime();
            result = fmtToShow.print(new DateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
