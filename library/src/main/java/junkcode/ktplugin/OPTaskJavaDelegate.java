package junkcode.ktplugin;

import com.squareup.kotlinpoet.FileSpec;

public class OPTaskJavaDelegate {

    public static FileSpec generateKTActivity(String namespace, JunkCodeConfig config, String packageName,
                                              String activityPreName, String layoutName) {
        return RealOPTask.INSTANCE.generateKTActivity(namespace, config, packageName,
                activityPreName, layoutName);
    }
}
