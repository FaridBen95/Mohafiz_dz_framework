package com.MohafizDZ.framework_repository.core;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.MohafizDZ.App;

import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashMap;

import dalvik.system.DexFile;

public class ModelRegistryUtils {

    private HashMap<String, Class<? extends Model>> models = new HashMap<>();

    public void makeReady(Context context) {
        try {
            DexFile dexFile = new DexFile(context.getPackageCodePath());
            for (Enumeration<String> item = dexFile.entries(); item.hasMoreElements(); ) {
                String element = item.nextElement();
                if (element.startsWith(App.class.getPackage().getName())) {
                    Class<? extends Model> clsName = (Class<? extends Model>) Class.forName(element);
                    if (clsName != null && clsName.getSuperclass() != null &&
                            Model.class.isAssignableFrom(clsName.getSuperclass())) {
                        String modelName = getModelName(context, clsName);
                        if (modelName != null) {
                            this.models.put(clsName.getName(), clsName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getModelName(Context context, Class cls) {
        try {
            Constructor constructor = cls.getConstructor(Context.class );
            Model model = (Model) constructor.newInstance(context);
            return model.getModelName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Class<? extends Model> getModel(String className) {
        if (models.containsKey(className)) {
            return models.get(className);
        }
        return null;
    }

    public HashMap<String, Class<? extends Model>> getModels() {
        return models;
    }
}
