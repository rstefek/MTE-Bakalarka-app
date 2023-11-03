package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import info.stefkovi.studium.mte_bakalarka.R;

public class SharedPreferencesHelper {

    private SharedPreferences _sharedPrefs;

    public SharedPreferencesHelper(Context ctx) {
        _sharedPrefs = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public void savePrefString(String key, String value) {        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
