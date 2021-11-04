package com.MohafizDZ.framework_repository.controls.codex;

public class RecyclerViewModel {
    //Required for both RecyclerViewAdapter1 and RecyclerViewAdapter2.
    private int image;
    private String name, location;

    //Required for both RecyclerViewAdapter2 only.
    private String quantity;

    public RecyclerViewModel() {
    }

    public RecyclerViewModel(int image, String name, String location, String quantity) {
        this.image = image;
        this.name = name;
        this.location = location;
        this.quantity = quantity;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
