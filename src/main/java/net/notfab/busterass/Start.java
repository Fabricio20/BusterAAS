package net.notfab.busterass;

import java.io.File;

public class Start {

    public static void main(String[] args) {
        File extrasFolder = new File("extras");
        System.setProperty("webdriver.opera.driver",
                new File(extrasFolder, "operadriver_win64\\operadriver.exe").getAbsolutePath());
        System.setProperty("webdriver.gecko.driver",
                new File(extrasFolder, "geckodriver_win64\\geckodriver.exe").getAbsolutePath());
        System.setProperty("webdriver.chrome.driver",
                new File(extrasFolder, "chromedriver_win32\\chromedriver.exe").getAbsolutePath());
        // --
//        new Buster()
//                .useChrome()
//                .run();
        new Main();
    }

}