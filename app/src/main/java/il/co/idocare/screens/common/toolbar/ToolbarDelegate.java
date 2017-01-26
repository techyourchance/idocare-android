package il.co.idocare.screens.common.toolbar;

/**
 * Implementations of this interface can be used in order to manipulate the state of the toolbar
 */

public interface ToolbarDelegate {

    void setTitle(String title);

    void showNavigateUpButton();

    void showNavDrawerButton();

    void hideToolbar();

    void showToolbar();
}
