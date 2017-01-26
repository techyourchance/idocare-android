package il.co.idocare.screens.common;

/**
 * This interface should be implemented by Activities and Fragments which represent a top level
 * element of user visible screen.
 */
public interface Screen {

    public static final int TOOLBAR_BUTTON_STATE_NAV_DRAWER = 1;
    public static final int TOOLBAR_BUTTON_STATE_UP = 2;

    /**
     * This method returns the state that describes which kind of functionality should toolbar's
     * "main" button have
     * @return either {@link #TOOLBAR_BUTTON_STATE_NAV_DRAWER} or {@link #TOOLBAR_BUTTON_STATE_UP}
     */
    int getToolbarButtonState();

    /**
     * This method is used to control toolbar visibility
     * @return true if the screen should be shown with toolbar; false otherwise
     */
    boolean shouldShowToolbar();

    /**
     * Get screen's title
     * @return screen's title, or empty string if the screen does not have a title
     */
    public String getTitle();


}
