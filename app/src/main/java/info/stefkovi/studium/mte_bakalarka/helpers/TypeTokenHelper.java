package info.stefkovi.studium.mte_bakalarka.helpers;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;

public class TypeTokenHelper {
    public static Type getCellListType() {
        return new TypeToken<ArrayList<CellInfoApiModel>>(){}.getType();
    }
}
