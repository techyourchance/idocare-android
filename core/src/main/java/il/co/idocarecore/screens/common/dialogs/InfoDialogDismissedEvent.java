package il.co.idocarecore.screens.common.dialogs;

import il.co.idocarecore.screens.common.dialogs.BaseDialogEvent;

/**
 * This event will be posted to EventBus when InfoDialog is dismissed
 */
public class InfoDialogDismissedEvent extends BaseDialogEvent {

    public InfoDialogDismissedEvent(String tag) {
        super(tag);
    }
}
