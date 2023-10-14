package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public static boolean AllPermissionsAccepted(Context ctx, String[] perms) {
        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}
