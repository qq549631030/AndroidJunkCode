package cn.hx.plugin.junkcode.template

class ManifestTemplate {
    static final def TEMPLATE = '''<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
    </application>
</manifest>'''

    static final def ACTIVITY_NODE = '''
        <activity android:name="${packageName}.${activityName}">
        </activity>'''
}