package com.MohafizDZ.framework_repository.local_sentry;

import java.util.List;

public interface GlobalTouchListener {
    String ClickedOn(int x, int y);

    boolean startTrack();

    boolean trackByTag(); // add track in the tag for each view

    List<Class<?>> trackByClass();

    List<String> trackVariables();
}
