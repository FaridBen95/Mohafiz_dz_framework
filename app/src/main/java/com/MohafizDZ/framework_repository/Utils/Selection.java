package com.MohafizDZ.framework_repository.Utils;

import java.util.List;

public class Selection {
    private String selection = "";
    private String[] args = {};

    public String getSelection() {
        return selection;
    }

    public String[] getArgs() {
        return args;
    }

    public void addSelection(String selection, String arg){
        addArg(arg);
        addSelection(selection);
    }

    public void addSelection(String selection){
        this.selection = this.selection.length() > 0? this.selection + " and " + selection : selection;
    }

    public void addArg(String arg){
        if(arg != null){
            args = MyUtil.addArgs(args, arg);
        }
    }

    public void addArgs(List<String> args){
        for(String arg : args){
            addArg(arg);
        }
    }
}
