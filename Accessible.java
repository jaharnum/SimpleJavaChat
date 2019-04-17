import javafx.scene.control.TextArea;
/*
File name:
Author: Jamie Harnum #040898399
Course: CST8221 - JAP, Lab Section 303
Assignment: 2
Date: April 17, 2019
Professor: Daniel Cormier
Purpose: Interface for necessary methods for each ChatUI
*/

/**
 Provides abstract methods for all ChatUIs to implement
 @author Jamie Harnum
 @version 1
 @since 1.8.0_144
 */
public interface Accessible {

    TextArea getDisplay();
    void closeChat();

}
