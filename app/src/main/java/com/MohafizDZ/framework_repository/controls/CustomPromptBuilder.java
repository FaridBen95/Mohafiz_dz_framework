package com.MohafizDZ.framework_repository.controls;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.MohafizDZ.App;

import uk.co.samuelwall.materialtaptargetprompt.ActivityResourceFinder;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.ResourceFinder;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptOptions;

public class CustomPromptBuilder extends PromptOptions<CustomPromptBuilder>
{
    private Activity activity;
    /**
     * The key to use in the shared preferences to check if the
     * prompt has already been shown.
     */
    @Nullable private String key;

    /**
     * Constructor.
     *
     * @param activity The activity to use to find resources.
     */
    public CustomPromptBuilder(final @NonNull Activity activity)
    {
        this(new ActivityResourceFinder(activity));
        this.activity = activity;
    }

    /**
     * Constructor.
     *
     * @param resourceFinder The resource finder implementation
     *  to use to find resources.
     */
    public CustomPromptBuilder(final @NonNull ResourceFinder resourceFinder)
    {
        super(resourceFinder);
    }

    /**
     * Set the key to use in the shared preferences.
     *
     * @param key Preferences key.
     * @return This Builder object to allow for chaining of calls to set methods
     */
    @NonNull
    public CustomPromptBuilder setPreferenceKey(@Nullable final String key)
    {
        this.key = key;
        return this;
    }

    @Nullable
    public MaterialTapTargetPrompt.Builder build()
    {
        final SharedPreferences sharedPreferences = this.getResourceFinder()
                .getContext()
                .getSharedPreferences("preferences", 0);
        if (this.key == null || !sharedPreferences.getBoolean(this.key, false))
        {
            if (!App.TEST_MODE && this.key != null)
            {
                sharedPreferences.edit().putBoolean(this.key, true).apply();
            }
            return new MaterialTapTargetPrompt.Builder(activity);
        }
        return null;
    }
}
