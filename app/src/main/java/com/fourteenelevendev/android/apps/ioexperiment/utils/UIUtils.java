package com.fourteenelevendev.android.apps.ioexperiment.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fourteenelevendev.android.apps.ioexperiment.R;
import com.fourteenelevendev.android.apps.ioexperiment.model.Letter;

/**
 * Basic utilities
 *
 * Created by kyleparker on 4/6/2016.
 */
public class UIUtils {

    /**
     * Generate random number
     * @param min
     * @param max
     * @return
     */
    public static int generateRandomInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    // TODO: Determine if this can be dynamic based on mathematically setting the start position based on the number of devices and
    // the current device count. At the moment, this is hard-coded since the letters/message are hard-coded and the letters should
    // appear in a particular sequence based on the number of devices

    /**
     * Generate the appropriate letter list based on the current device number
     * @param deviceNumber
     * @param totalDevices
     * @return
     */
    public static List<Letter> generateLetterList(int deviceNumber, int totalDevices) {
        List<Letter> list = new ArrayList<>();

        Letter letter1 = new Letter();
        letter1.setImageResId(R.drawable.img_big_g);
        letter1.setContentDescription(R.string.message_g);

        Letter letter2 = new Letter();
        letter2.setImageResId(R.drawable.img_o1);
        letter2.setContentDescription(R.string.message_o);

        Letter letter3 = new Letter();
        letter3.setImageResId(R.drawable.img_o2);
        letter3.setContentDescription(R.string.message_o);

        Letter letter4 = new Letter();
        letter4.setImageResId(R.drawable.img_g);
        letter4.setContentDescription(R.string.message_g);

        Letter letter5 = new Letter();
        letter5.setImageResId(R.drawable.img_l);
        letter5.setContentDescription(R.string.message_l);

        Letter letter6 = new Letter();
        letter6.setImageResId(R.drawable.img_e);
        letter6.setContentDescription(R.string.message_e);

        Letter letter7 = new Letter();
        letter7.setImageResId(R.drawable.img_big_i);
        letter7.setContentDescription(R.string.message_i);

        Letter letter8 = new Letter();
        letter8.setImageResId(R.drawable.img_slash);
        letter8.setContentDescription(R.string.message_slash);

        Letter letter9 = new Letter();
        letter9.setImageResId(R.drawable.img_big_o);
        letter9.setContentDescription(R.string.message_o);

        Letter letter10 = new Letter();
        letter10.setImageResId(R.drawable.img_one);
        letter10.setContentDescription(R.string.message_one);

        Letter letter11 = new Letter();
        letter11.setImageResId(R.drawable.img_six);
        letter11.setContentDescription(R.string.message_six);

        switch (totalDevices) {
            case 1:
                list.add(0, letter1);
                list.add(1, letter2);
                list.add(2, letter3);
                list.add(3, letter4);
                list.add(4, letter5);
                list.add(5, letter6);
                list.add(6, letter7);
                list.add(7, letter8);
                list.add(8, letter9);
                list.add(9, letter10);
                list.add(10, letter11);
                break;
            case 2:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                    list.add(1, letter2);
                    list.add(2, letter3);
                    list.add(3, letter4);
                    list.add(4, letter5);
                    list.add(5, letter6);
                } else {
                    list.add(0, letter7);
                    list.add(1, letter8);
                    list.add(2, letter9);
                    list.add(3, letter10);
                    list.add(4, letter11);
                }
                break;
            case 3:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                    list.add(1, letter2);
                    list.add(2, letter3);
                    list.add(3, letter4);
                    list.add(4, letter5);
                    list.add(5, letter6);
                } else if (deviceNumber == 2) {
                    list.add(0, letter7);
                    list.add(1, letter8);
                    list.add(2, letter9);
                } else {
                    list.add(0, letter10);
                    list.add(1, letter11);
                }
                break;
            case 4:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                    list.add(1, letter2);
                    list.add(2, letter3);
                } else if (deviceNumber == 2) {
                    list.add(0, letter4);
                    list.add(1, letter5);
                    list.add(2, letter6);
                } else if (deviceNumber == 3) {
                    list.add(0, letter7);
                    list.add(1, letter8);
                    list.add(2, letter9);
                } else {
                    list.add(0, letter10);
                    list.add(1, letter11);
                }
                break;
            case 5:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                    list.add(1, letter2);
                } else if (deviceNumber == 2) {
                    list.add(0, letter3);
                    list.add(1, letter4);
                } else if (deviceNumber == 3) {
                    list.add(0, letter5);
                    list.add(1, letter6);
                } else if (deviceNumber == 4) {
                    list.add(0, letter7);
                    list.add(1, letter8);
                    list.add(2, letter9);
                } else {
                    list.add(0, letter10);
                    list.add(1, letter11);
                }
                break;
            case 6:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                    list.add(1, letter2);
                } else if (deviceNumber == 2) {
                    list.add(0, letter3);
                    list.add(1, letter4);
                } else if (deviceNumber == 3) {
                    list.add(0, letter5);
                    list.add(1, letter6);
                } else if (deviceNumber == 4) {
                    list.add(0, letter7);
                    list.add(1, letter8);
                    list.add(2, letter9);
                } else if (deviceNumber == 5) {
                    list.add(0, letter10);
                } else {
                    list.add(0, letter11);
                }
                break;
            case 7:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                    list.add(1, letter2);
                } else if (deviceNumber == 2) {
                    list.add(0, letter3);
                    list.add(1, letter4);
                } else if (deviceNumber == 3) {
                    list.add(0, letter5);
                    list.add(1, letter6);
                } else if (deviceNumber == 4) {
                    list.add(0, letter7);
                } else if (deviceNumber == 5) {
                    list.add(0, letter8);
                } else if (deviceNumber == 6) {
                    list.add(0, letter9);
                } else {
                    list.add(0, letter10);
                    list.add(1, letter11);
                }
                break;
            case 8:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                } else if (deviceNumber == 2) {
                    list.add(0, letter2);
                } else if (deviceNumber == 3) {
                    list.add(0, letter3);
                } else if (deviceNumber == 4) {
                    list.add(0, letter4);
                } else if (deviceNumber == 5) {
                    list.add(0, letter5);
                } else if (deviceNumber == 6) {
                    list.add(0, letter6);
                } else if (deviceNumber == 7) {
                    list.add(0, letter7);
                    list.add(1, letter8);
                    list.add(2, letter9);
                } else {
                    list.add(0, letter10);
                    list.add(1, letter11);
                }
                break;
            case 9:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                } else if (deviceNumber == 2) {
                    list.add(0, letter2);
                } else if (deviceNumber == 3) {
                    list.add(0, letter3);
                } else if (deviceNumber == 4) {
                    list.add(0, letter4);
                } else if (deviceNumber == 5) {
                    list.add(0, letter5);
                } else if (deviceNumber == 6) {
                    list.add(0, letter6);
                } else if (deviceNumber == 7) {
                    list.add(0, letter7);
                    list.add(1, letter8);
                    list.add(2, letter9);
                } else if (deviceNumber == 8) {
                    list.add(0, letter10);
                } else {
                    list.add(0, letter11);
                }
                break;
            case 10:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                } else if (deviceNumber == 2) {
                    list.add(0, letter2);
                } else if (deviceNumber == 3) {
                    list.add(0, letter3);
                } else if (deviceNumber == 4) {
                    list.add(0, letter4);
                } else if (deviceNumber == 5) {
                    list.add(0, letter5);
                } else if (deviceNumber == 6) {
                    list.add(0, letter6);
                } else if (deviceNumber == 7) {
                    list.add(0, letter7);
                } else if (deviceNumber == 8) {
                    list.add(0, letter8);
                } else if (deviceNumber == 9) {
                    list.add(0, letter9);
                } else {
                    list.add(0, letter10);
                    list.add(1, letter11);
                }
                break;
            case 11:
                if (deviceNumber == 1) {
                    list.add(0, letter1);
                } else if (deviceNumber == 2) {
                    list.add(0, letter2);
                } else if (deviceNumber == 3) {
                    list.add(0, letter3);
                } else if (deviceNumber == 4) {
                    list.add(0, letter4);
                } else if (deviceNumber == 5) {
                    list.add(0, letter5);
                } else if (deviceNumber == 6) {
                    list.add(0, letter6);
                } else if (deviceNumber == 7) {
                    list.add(0, letter7);
                } else if (deviceNumber == 8) {
                    list.add(0, letter8);
                } else if (deviceNumber == 9) {
                    list.add(0, letter9);
                } else if (deviceNumber == 10) {
                    list.add(0, letter10);
                } else {
                    list.add(0, letter11);
                }
                break;
        }

        return list;
    }
}
