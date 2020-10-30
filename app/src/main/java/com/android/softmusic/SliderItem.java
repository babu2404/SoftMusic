package com.android.softmusic;

public class SliderItem {

    //change this from int to string ....because now we will retrieve url in string format

    // private int image;
    private String image;
   /* public SliderItem(int image) {
        this.image = image;
    }*/
   public SliderItem(String image) {
       this.image = image;
   }

    public String getImage() {
        return image;
    }
}
